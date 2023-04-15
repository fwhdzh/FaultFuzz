package edu.iscas.CCrashFuzzer.control.determine;

import java.util.ArrayList;

import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.Fuzzer;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.control.AbstractDeterminismTarget;
import edu.iscas.CCrashFuzzer.control.AbstractTarget;
import edu.iscas.CCrashFuzzer.control.determine.TryBestDeterminismController.TryBestDeterminismControllerResult;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class TryBestDeterminismTarget extends AbstractDeterminismTarget{

    private TryBestDeterminismControllerResult controllerResult;

    public static class TryBestDeterminismTResult {
		public int result;
	}

    private TryBestDeterminismTResult mResult;
    private boolean finishWorkload;
    
    @Override
    public void doTarget() {
        // TODO Auto-generated method stub
        runATestWithTryBestDetermineControl(mSeqPair, mConf, mTestID, mWaitSeconds);
    }

    @Override
    public Object afterTarget() {
        // TODO Auto-generated method stub
        sendNotReplayToCluster(controllerResult.finalCluster);
		// For replay target, we should collect run-time information for all scenarios.
		String runInfoPath = collectRuntimeInfo(controllerResult.finalCluster);
		boolean findBug = checkIfABugExist(runInfoPath);
		mResult = generateTryBestDeterminismTResult(finishWorkload, controllerResult.allFaultsAreInjected , findBug);
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
		return result;
	}

}
