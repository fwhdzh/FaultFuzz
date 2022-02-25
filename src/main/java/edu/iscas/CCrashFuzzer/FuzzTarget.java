package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class FuzzTarget extends AbstractFuzzTarget{
	ArrayList<String> logInfo;
	ArrayList<String> checkInfo;
	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	public int run_target(FaultSequence seq, Conf conf, String testID) {
		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(seq.toString()));
		logInfo.add(Stat.log("}"));
		
		int rst = -1;
		rst = runATest(seq,conf,testID);
		
		logInfo.add(Stat.log("Finish "+testID+"th test, test result is:"+rst
				+". (0: triggered-no-bug; 1: triggered-bug; -1: not-triggered)"));
		return rst;
	}

	//0 triggered, no bug
    //1 triggered, bug
	//-1 not triggered
	public int runATest(FaultSequence seq, Conf conf, String testID) {
		int ret = -1;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		Controller controller = new Controller(new Cluster(conf), conf.CONTROLLER_PORT, conf);
//        controller.prepareFaultSeq(FaultSequence.getEmptyIns());//keep curCrash null
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(controller.cluster.prepareCluster());
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		controller.prepareFaultSeq(seq);
		logInfo.add(Stat.log("Start controller ..."));
		controller.startController();
		
		logInfo.add(Stat.log("Waiting for alive controller server thread ..."));
		while(!controller.serverThread.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//start the cluster
		//run the test case
		logInfo.add(Stat.log("Run workload ..."));
		Thread runWorkload = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				logInfo.addAll(controller.cluster.runWorkload());
				logInfo.add(Stat.log("The workload was finished."));
			}
		};
		runWorkload.start();
		
		int waitIdx = 0;
		boolean addController = false;
		while(!controller.faultInjected || runWorkload.isAlive()) {
		    try {
                Thread.sleep(1000);
                waitIdx++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		    if(waitIdx > (conf.hangMinutes*60)) {
            	if(!controller.faultInjected) {
            		logInfo.addAll(controller.rst);
    		    	logInfo.add(Stat.log("Exit abnormally, stop controller ..."));
            		controller.stopController();
            		addController = true;
            		return -1;
            	} else if (controller.faultInjected && runWorkload.isAlive()) {
            		logInfo.addAll(controller.rst);
            		logInfo.add(Stat.log("FAV test has failed: the run did not finished in "+conf.hangMinutes+" minutes."));
            		ret = 2;
            		addController = true;
            		break;
            	}
            }
		}

		if(!addController) {
			logInfo.addAll(controller.rst);
		}
		
		logInfo.add(Stat.log("Command to save run-time traces ..."));
		List<Thread> saveTraceThs = new ArrayList<Thread>();
		for(MaxDownNodes subCluster:controller.currentCluster) {
			for(String alive:subCluster.aliveGroup) {
				Thread t = new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						String[] args = new String[2];
						args[0] = alive;
						args[1] = String.valueOf(conf.AFL_PORT);
						try {
							AflCli.main(args);
						} catch (AflException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				};
				t.start();
				saveTraceThs.add(t);
			}
		}
		for(Thread t:saveTraceThs) {
			try {
				t.join(600000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logInfo.add(Stat.log("Finish saving run-time traces."));
		
		logInfo.add(Stat.log("Collecting run-time information ..."));
		Monitor m = new Monitor(conf);
		String runInfoPath = m.getTmpReportDir(testID);
		m.collectRunTimeInfo(runInfoPath);
		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), runInfoPath);
		
		logInfo.add(Stat.log("Going to check the system. Faults injected: "+seq.toString()));
		logInfo.addAll(controller.cluster.runChecker(seq, conf, controller.currentCluster, runInfoPath+FileUtil.monitorName));
		int checkBug = checkBug(seq, conf);
		
		logInfo.add(Stat.log("Exit normally, stop controller ..."));
		controller.stopController();
		if(ret == 2) {
			return 2;
		} else {
			return checkBug;
		}
	}

	private int checkBug(FaultSequence seq, Conf conf) {
		// TODO Auto-generated method stub
		for(int i = 0; i<logInfo.size(); i++) {
            String s = logInfo.get(i);
            if(s.contains("FAV test has failed")) {
                this.checkInfo.add(s);
            }
        }
		return (this.checkInfo.size()>0)? 1:0;
	}
}
