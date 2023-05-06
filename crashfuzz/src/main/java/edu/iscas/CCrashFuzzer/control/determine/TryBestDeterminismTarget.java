package edu.iscas.CCrashFuzzer.control.determine;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.AflCli;
import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.Fuzzer;
import edu.iscas.CCrashFuzzer.RunCommand;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.control.AbstractDeterminismTarget;
import edu.iscas.CCrashFuzzer.control.determine.TryBestDeterminismController.TryBestDeterminismControllerResult;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class TryBestDeterminismTarget extends AbstractDeterminismTarget{

    private TryBestDeterminismControllerResult controllerResult;

    public static class TryBestDeterminismTResult {
		public int result;
		public List<String> logInfo;
	}

    private TryBestDeterminismTResult mResult;
    private boolean finishWorkload;

	
    
    @Override
	public void beforeTarget(FaultSeqAndIOSeq seqPair, Conf conf, String testID, long waitSeconds) {
		// TODO Auto-generated method stub
		super.beforeTarget(seqPair, conf, testID, waitSeconds);

		
	}

	@Override
    public void doTarget() {
        // TODO Auto-generated method stub
        runATestWithTryBestDetermineControl(mSeqPair, mConf, mTestID, mWaitSeconds);
    }

    @Override
    public TryBestDeterminismTResult afterTarget() {
        // TODO Auto-generated method stub
        // sendNotReplayToCluster(controllerResult.finalCluster);
		AflCli.executeUtilSuccess(controllerResult.finalCluster, mConf, AflCommand.DETERMINE_NO_SEND, 300000);
		// For replay target, we should collect run-time information for all scenarios.
		String runInfoPath = collectRuntimeInfo(controllerResult.finalCluster);
		copyLogsToControllerWithTestId(mTestID);
		boolean findBug = checkIfABugExist(runInfoPath);
		mResult = generateTryBestDeterminismTResult(finishWorkload, controllerResult.allFaultsAreInjected , findBug);
		Stat.log("TryBestDeterminismTResult is "+mResult.result);
		return mResult;
    }

    private int runATestWithTryBestDetermineControl(FaultSeqAndIOSeq seqPair, final Conf conf, String testID, long waitSeconds) {
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(seqPair.faultSeq.toString()));
		logInfo.add(Stat.log("}"));

		

		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		final TryBestDeterminismController tbdController = new TryBestDeterminismController(mCluster, conf.CONTROLLER_PORT, conf);
		// final ReplayController dController = new ReplayController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(tbdController.cluster.prepareCluster());
		tbdController.cluster.copyEnvToCluster();
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		// dController.prepareFaultSeq(seq);
		tbdController.prepareFaultSeqAndIOSeq(seqPair);
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
				logInfo.addAll(tbdController.cluster.runWorkload());
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
				Stat.log("waitIdx: " + waitIdx);
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

		// terriable implementation for thread synchronous
		int workloadIdx = 0;
		int workloadSecond = 20;
		while (tbdController.finishFlag == true && runWorkload.isAlive() && workloadIdx < workloadSecond) {
			Stat.log("Try to wait workload to finish ...");
			try {
				Thread.sleep(1000);
				workloadIdx++;
				Stat.log("workloadIdx: " + workloadIdx);
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
			logInfo.addAll(mCluster.runChecker(mConf, controllerResult.finalCluster, runInfoPath+FileUtil.monitorDir));
			// logInfo.addAll(dController.cluster.runChecker(conf, dController.currentCluster, runInfoPath+FileUtil.monitorDir));
			int checkBugRst = checkBug(seq, mConf);
			if (checkBugRst == 1) {
				result = true;
			}
			logInfo.add(Stat.log("Exit normally, stop controller ..."));
		}
		return result;
	}

    public TryBestDeterminismTResult generateTryBestDeterminismTResult(boolean workloadFinish, boolean injectFaultFinish, boolean findBug) {
		TryBestDeterminismTResult result = new TryBestDeterminismTResult();
		Stat.log("workloadFinish: " + workloadFinish + ", injectFaultFinish: " + injectFaultFinish + ", findBug: " + findBug);
		if (workloadFinish && injectFaultFinish && findBug) {
			result.result = 0;
		} else if (workloadFinish && injectFaultFinish && !findBug) {
			result.result = 1;
		} else if (workloadFinish && !injectFaultFinish) {
			result.result = 2;
		} else if (!workloadFinish && injectFaultFinish) {
			result.result = 3;
		} else if (!workloadFinish && !injectFaultFinish) {
			result.result = -1;
		}
		result.logInfo = logInfo;
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

	public ArrayList<String> copyLogsToController(String logsDir) {
		if (mConf.COPY_LOGS_TO_CONTROLLER != null) {
			String path = mConf.COPY_LOGS_TO_CONTROLLER.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path + " " + logsDir, workingDir);
		} else {
			return null;
		}
	}

	public ArrayList<String> copyLogsToControllerWithTestId(String testID) {
		ArrayList<String> result = null;
		result = copyLogsToController(mConf.CLUSTER_LOGS_IN_CONTROLLER_DIR + "/" + testID);
		return result;
	}

}
