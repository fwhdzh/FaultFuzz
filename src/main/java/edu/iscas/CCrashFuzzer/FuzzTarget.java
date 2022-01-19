package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuzzTarget extends AbstractFuzzTarget{
	ArrayList<String> logInfo;
	ArrayList<String> checkInfo;
	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	public int run_target(FaultSequence seq, Conf conf, long run, int maxTries) {
		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		
		logInfo.add(Stat.log("=========================Run the target for the "+run+"th"+" time========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(seq.toString()));
		logInfo.add(Stat.log("}"));
		int tries = 0;
		int rst = -1;
		while(tries < maxTries) {
			logInfo.add(Stat.log("=========================Going to start"+tries+"th run========================="));
			rst = runATest(seq,conf);
			System.out.println("");
			tries++;
			if(rst > -1) {
				break;
			}
		}
		logInfo.add(Stat.log("Finish testing!"));
		return rst;
	}

	//0 triggered, no bug
    //1 triggered, bug
	//-1 not triggered
	public int runATest(FaultSequence seq, Conf conf) {
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
		logInfo.add(Stat.log("Start test case ..."));
		Thread runWorkload = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				logInfo.addAll(controller.cluster.runWorkload());
				logInfo.add(Stat.log("The test case was finished."));
			}
		};
		runWorkload.start();
		
		int waitIdx = 0;
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
            		return -1;
            	} else if (controller.faultInjected && runWorkload.isAlive()) {
            		logInfo.add(Stat.log("FAV test has failed: the run did not finished in "+conf.hangMinutes+" minutes."));
            		ret = 2;
            		break;
            	}
            }
		}

		logInfo.addAll(controller.rst);
		
		logInfo.add(Stat.log("Going to check the system. Faults injected: "+seq.toString()));
		logInfo.addAll(controller.cluster.runChecker(seq, conf));
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
