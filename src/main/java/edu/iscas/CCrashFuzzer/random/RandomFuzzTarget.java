package edu.iscas.CCrashFuzzer.random;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.AbstractFuzzTarget;
import edu.iscas.CCrashFuzzer.AflCli;
import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.Controller;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.Fuzzer;
import edu.iscas.CCrashFuzzer.Monitor;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class RandomFuzzTarget {
	ArrayList<String> logInfo;
	ArrayList<String> checkInfo;
	long a_exec_seconds;
	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	public int run_target(RandomFaultSequence seq, Conf conf, String testID, long waitSeconds) {
		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;
		
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(seq.toString()));
		logInfo.add(Stat.log("}"));
		
		int rst = -1;
		rst = runATest(seq,conf,testID,waitSeconds);
		
		logInfo.add(Stat.log("Finish "+testID+"th test, test result is:"+rst
				+". (0: triggered-no-bug; 1: triggered-bug; 2: triggered-hang; -1: not-triggered)"));
		return rst;
	}

	//0 triggered, no bug
    //1 triggered, bug
	//2 triggered, hang
	//-1 not triggered
	public int runATest(RandomFaultSequence seq, Conf conf, String testID, long waitSeconds) {
		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		RandomController controller = new RandomController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
//        controller.prepareFaultSeq(FaultSequence.getEmptyIns());//keep curCrash null
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(controller.cluster.prepareCluster());
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		controller.prepareFaultSeq(seq);
		
		//start the cluster
		//run the test case
		logInfo.add(Stat.log("Run workload ..."));
		Thread runWorkload = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				logInfo.add(Stat.log("The workload is running ..."));
				logInfo.addAll(controller.cluster.runWorkload());
				logInfo.add(Stat.log("The workload was finished."));
			}
		};
		long start = System.currentTimeMillis();
		runWorkload.start();

		logInfo.add(Stat.log("Start controller ..."));
		controller.startController();
		
		int waitIdx = 0;
		boolean addedController = false;
		do {
			if(controller.injectionAborted) {
				ret = -1;
				addedController = true;
				logInfo.addAll(controller.rst);
		    	logInfo.add(Stat.log("Exit abnormally since current fault sequence was aborted, stop controller ..."));
        		break;
			}
		    try {
                Thread.sleep(1000);
                waitIdx++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		    if(waitIdx > waitSeconds) {
            	if(!controller.faultInjected) {
            		logInfo.addAll(controller.rst);
    		    	logInfo.add(Stat.log("Exit abnormally after waiting "+waitSeconds+" seconds, stop controller ..."));
    		    	addedController = true;
    		    	ret = -1;
    		    	break;
            	} else if (controller.faultInjected && runWorkload.isAlive()) {
            		logInfo.addAll(controller.rst);
            		logInfo.add(Stat.log("FAV test has failed: the run did not finished in "+waitSeconds+" seconds."));
            		ret = 2;
            		addedController = true;
            		break;
            	}
            }
		} while(!controller.faultInjected || runWorkload.isAlive());
		a_exec_seconds = Fuzzer.getExecSeconds(start);

		if(!addedController) {
			logInfo.addAll(controller.rst);
		}
		
		if(ret != -1) {// not need to wait not triggered cases
			//wait recovery process finish
			logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
			List<Thread> waitRecoveryTds = new ArrayList<Thread>();
			for(MaxDownNodes subCluster:controller.currentCluster) {
				for(String alive:subCluster.aliveGroup) {
					Thread t = new Thread() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							String[] args = new String[3];
							args[0] = alive;
							args[1] = String.valueOf(conf.AFL_PORT);
							args[2] = AflCommand.STABLE.toString();
							try {
								AflCli.main(args);
							} catch (AflException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					};
					t.start();
					waitRecoveryTds.add(t);
				}
			}
			for(Thread t:waitRecoveryTds) {
				try {
					t.join(300000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logInfo.add(Stat.log("Finish waiting recovery processes."));
			
			logInfo.add(Stat.log("Command to save run-time traces ..."));
			List<Thread> saveTraceThs = new ArrayList<Thread>();
			for(MaxDownNodes subCluster:controller.currentCluster) {
				for(String alive:subCluster.aliveGroup) {
					Thread t = new Thread() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							String[] args = new String[3];
							args[0] = alive;
							args[1] = String.valueOf(conf.AFL_PORT);
							args[2] = AflCommand.SAVE.toString();
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
		}

		Monitor m = new Monitor(conf);
		String runInfoPath = m.getTmpReportDir(testID);
		if(ret != -1) {//no need to collect traces and logs for not triggered ones
			logInfo.add(Stat.log("Collecting run-time information ..."));
			m.collectRunTimeInfo(runInfoPath);
			FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), runInfoPath);
		}
		
		if(ret == 0) {
			logInfo.add(Stat.log("Going to check the system. Faults injected: "+seq.toString()));
			logInfo.addAll(controller.cluster.runChecker(conf, controller.currentCluster, runInfoPath+FileUtil.monitorDir));
			ret = checkBug(seq, conf);
			logInfo.add(Stat.log("Exit normally, stop controller ..."));
		}
		
		controller.stopController();
		return ret;
	}

	private int checkBug(RandomFaultSequence seq, Conf conf) {
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
