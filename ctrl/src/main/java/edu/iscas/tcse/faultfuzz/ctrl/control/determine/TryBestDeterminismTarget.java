package edu.iscas.tcse.faultfuzz.ctrl.control.determine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Fuzzer;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.RunCommand;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractDeterminismTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.determine.TryBestDeterminismController.TryBestDeterminismControllerResult;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class TryBestDeterminismTarget extends AbstractDeterminismTarget{

    private TryBestDeterminismControllerResult controllerResult;

    public static class TryBestDeterminismTResult {
		public int resultCode;
		public List<String> logInfo;

		public long exec_time;
		public List<FaultPoint> injectedFaultPointList;
	}

    private TryBestDeterminismTResult mResult;
    private boolean finishWorkload;

	public int controllerPort;
	public List<MaxDownNodes> maxDownGroup;
	public int determineWaitTime;

	
    
	public void beforeTarget(QueueEntryRuntime entryRuntime, Cluster cluster, int aflPort, File curFaultFile, File monitor, String testID, long waitSeconds, int controllerPort, List<MaxDownNodes> maxDownGroup, int determineWaitTime) {
		
		// Cluster cluster = new Cluster(conf);
		// int aflPort = conf.AFL_PORT;
		// File curFaultFile = conf.CUR_FAULT_FILE;
		// File monitor = conf.MONITOR;

		
		super.beforeTarget(entryRuntime, cluster, aflPort, curFaultFile, monitor, testID, waitSeconds);		
		
		this.controllerPort = controllerPort;
		this.maxDownGroup = maxDownGroup;
		this.determineWaitTime = determineWaitTime;
	}

	@Override
    public void doTarget() {
        runATestWithTryBestDetermineControl(mSeqPair, controllerPort, curFaultFile, maxDownGroup, aflPort, determineWaitTime, mTestID, mWaitSeconds);
    }

    @Override
    public TryBestDeterminismTResult afterTarget() {
        // TODO Auto-generated method stub
        // sendNotReplayToCluster(controllerResult.finalCluster);
		AflCli.executeUtilSuccess(controllerResult.finalCluster, aflPort, AflCommand.DETERMINE_NO_SEND, 300000);
		// For replay target, we should collect run-time information for all scenarios.
		String runInfoPath = collectRuntimeInfo(controllerResult.finalCluster);
		// copyLogsToControllerWithTestId(mTestID);
		boolean findBug = checkIfABugExist(runInfoPath);
		mResult = generateTryBestDeterminismTResult(finishWorkload, controllerResult.allFaultsAreInjected , findBug, controllerResult.injectedFaultPointList);
		Stat.log("TryBestDeterminismTResult is "+mResult.resultCode);
		Stat.log("a_exec_seconds is "+a_exec_seconds + "seconds");
		return mResult;
    }

    private int runATestWithTryBestDetermineControl(QueueEntryRuntime entryRuntime, int controllerPort, File curFaultFile, List<MaxDownNodes> maxDownGroup, int aflPort, int determineWaitTime, String testID, long waitSeconds) {
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(entryRuntime.faultSeq.toString()));
		logInfo.add(Stat.log("}"));

		

		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		// final TryBestDeterminismController tbdController = new TryBestDeterminismController(mCluster, conf.CONTROLLER_PORT, conf);
		final TryBestDeterminismController tbdController = new TryBestDeterminismController(mCluster, controllerPort, curFaultFile, maxDownGroup, aflPort, determineWaitTime);
		// final ReplayController dController = new ReplayController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(tbdController.cluster.prepareCluster());
		// tbdController.cluster.copyEnvToCluster();
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		// dController.prepareFaultSeq(seq);
		tbdController.prepareFaultSeqAndIOSeq(entryRuntime);
		logInfo.add(Stat.log("Start controller ..."));
		tbdController.startController();
		
		logInfo.add(Stat.log("Waiting for alive controller server thread ..."));
		while(!tbdController.serverThread.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// executeCliCommandToCluster(tbdController.currentCluster, conf, AflCli.AflCommand.DOREPLAY, mWaitSeconds);
		
		//start the cluster
		//run the test case
		logInfo.add(Stat.log("Run workload ..."));
		Thread runWorkload = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				logInfo.add(Stat.log("The workload is running ..."));
				// logInfo.addAll(tbdController.cluster.runWorkload());
				List<String> workloadLogs = tbdController.cluster.runWorkload();
				logInfo.addAll(workloadLogs);
				logInfo.add(Stat.log("The workload was finished."));
			}
		};
		long start = System.currentTimeMillis();
		runWorkload.start();

		// logInfo.add(Stat.log("Check heartbeat ..."));
		// AflCli.executeUtilSuccess(tbdController.currentCluster, conf, AflCommand.HEARTBEAT, waitSeconds);
		
		int waitIdx = 0;
		while ((runWorkload.isAlive() || tbdController.finishFlag == false) && waitIdx < waitSeconds) {
			try {
                Thread.sleep(1000);
                waitIdx++;
				Stat.debug("waitIdx: " + waitIdx);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		Stat.log("The first while loop end! runWorkload.isAlive(): " + runWorkload.isAlive()
				+ " tbdController.finishFlag: " + tbdController.finishFlag + " waitIdx: " + waitIdx);

		// try {
		// 	Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }

		// wait for workload finish
		// terriable implementation for thread synchronous
		int workloadIdx = 0;
		int workloadSecond = 20;
		while (tbdController.finishFlag == true && runWorkload.isAlive() && workloadIdx < workloadSecond) {
			Stat.log("Try to wait workload to finish ...");
			try {
				Thread.sleep(1000);
				workloadIdx++;
				Stat.debug("workloadIdx: " + workloadIdx);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Stat.log("The second while loop end! runWorkload.isAlive(): " + runWorkload.isAlive()
				+ " tbdController.finishFlag: " + tbdController.finishFlag + " workloadIdx: " + workloadIdx);

		tbdController.stopController();
		/*
		 * Collect the result of replay controller and the result of workload
		 */
		controllerResult = tbdController.collectTryBestDeterminismControllerResult();
		finishWorkload = !runWorkload.isAlive();
		if (!controllerResult.allFaultsAreInjected) {
			logInfo.addAll(tbdController.rst);
		}
		a_exec_seconds = Fuzzer.getExecSeconds(start);
		Stat.log("workload end");

		return ret;
	}

    /*
     * Different from Replay.target checkIfABugExist!
     * Use allFaultsAreInjected instead of allPointsAreReplayed!
     */
    private boolean checkIfABugExist(String runInfoPath) {
		boolean result = false;
		if (controllerResult.allFaultsAreInjected) {
			FaultSequence seq = mSeqPair.faultSeq;
			logInfo.add(Stat.log("Going to check the system. Faults injected: "+seq.toString()));
			logInfo.addAll(mCluster.runChecker(controllerResult.finalCluster, runInfoPath+FileUtil.monitorDir));
			// logInfo.addAll(dController.cluster.runChecker(conf, dController.currentCluster, runInfoPath+FileUtil.monitorDir));
			int checkBugRst = checkBug(seq);
			if (checkBugRst == 1) {
				result = true;
			}
			logInfo.add(Stat.log("Exit normally, stop controller ..."));
		}
		return result;
	}

	
    public TryBestDeterminismTResult generateTryBestDeterminismTResult(boolean workloadFinish, boolean injectFaultFinish, boolean findBug, List<FaultPoint> injectedFaultPointList) {
		TryBestDeterminismTResult result = new TryBestDeterminismTResult();
		// Stat.log("workloadFinish: " + workloadFinish + ", injectFaultFinish: " + injectFaultFinish + ", findBug: " + findBug);
		// if (workloadFinish && injectFaultFinish && findBug) {
		// 	result.resultCode = 0;
		// } else if (workloadFinish && injectFaultFinish && !findBug) {
		// 	result.resultCode = 1;
		// } else if (workloadFinish && !injectFaultFinish) {
		// 	result.resultCode = 2;
		// } else if (!workloadFinish && injectFaultFinish) {
		// 	result.resultCode = 3;
		// } else if (!workloadFinish && !injectFaultFinish) {
		// 	result.resultCode = -1;
		// }
		result.resultCode = generateResultCode(workloadFinish, injectFaultFinish, findBug);
		result.logInfo = logInfo;

		result.injectedFaultPointList = injectedFaultPointList;
		result.exec_time = a_exec_seconds;

		return result;
	}

	private int generateResultCode(boolean workloadFinish, boolean injectFaultFinish, boolean findBug) {
		Stat.log("workloadFinish: " + workloadFinish + ", injectFaultFinish: " + injectFaultFinish + ", findBug: " + findBug);
		int result = -1;
		if (workloadFinish && injectFaultFinish && findBug) {
			result = 0;
		} else if (workloadFinish && injectFaultFinish && !findBug) {
			result = 1;
		} else if (workloadFinish && !injectFaultFinish) {
			result = 2;
		} else if (!workloadFinish && injectFaultFinish) {
			result = 3;
		} else if (!workloadFinish && !injectFaultFinish) {
			result = -1;
		}
		return result;
	}

	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	public static int mapTBDResutToFaultMode(int result) {
		int faultMode = -1;
		if (result == 0) {
			faultMode = 1;
		}
		if (result == 2 ||  result == -1) {
			faultMode = -1;
		}
		if (result == 3) {
			faultMode = 2;
		}
		if (result == 1) {
			faultMode = 0;
		}
		return faultMode;
	}

	public ArrayList<String> copyLogsToController(File copyLogsToController, String logsDir) {
		if (copyLogsToController != null) {
			String path = copyLogsToController.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path + " " + logsDir, workingDir);
		} else {
			return null;
		}
	}

	public ArrayList<String> copyLogsToControllerWithTestId(File copyLogsToController, String clusterLogsInConterllerDir, String testID) {
		ArrayList<String> result = null;
		result = copyLogsToController(copyLogsToController, clusterLogsInConterllerDir + "/" + testID);
		return result;
	}

}
