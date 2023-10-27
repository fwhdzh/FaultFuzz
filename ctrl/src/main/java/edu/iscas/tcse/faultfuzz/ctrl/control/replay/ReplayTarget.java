package edu.iscas.tcse.faultfuzz.ctrl.control.replay;

import java.io.File;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Fuzzer;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractDeterminismTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayController.ReplayControllerResult;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class ReplayTarget extends AbstractDeterminismTarget{



	// variables conncecting doTarget and afterTarget 
	private ReplayControllerResult controllerResult;
	private boolean finishWorkload;

	// target result
	private ReplayResult mResult;

	int controllerPort;
	List<MaxDownNodes> maxDownGroup;

	public static class ReplayResult {
		public int result;
		public String info;
	}

	// @Override
	// public void beforeTarget(Object data, Conf conf, Object... args) {
	// 	// TODO Auto-generated method stub
	// 	mEntry = (QueueEntry) data;
	// 	mConf = conf;
	// 	if (args.length == 2) {
	// 		mTestID = (String) args[0];
	// 		mWaitSeconds = (Long) args[1];
	// 	}
	// 	else {
	// 		throw new IllegalArgumentException("replay target args should be 2");
	// 	}

	// 	logInfo = new ArrayList<String>();
	// 	checkInfo = new ArrayList<String>();
	// 	a_exec_seconds = 0;

	// 	mCluster = new Cluster(conf);
	// }

	public void beforeTarget(QueueEntryRuntime seqPair, Cluster cluster, int aflPort, File curFaultFile, File monitor, String testID, long waitSeconds, int controllerPort, List<MaxDownNodes> maxDownGroup) {
		
		// Cluster cluster = new Cluster(conf);
		// int aflPort = conf.AFL_PORT;
		// File curFaultFile = conf.CUR_FAULT_FILE;
		// File monitor = conf.MONITOR;
		
		super.beforeTarget(seqPair, cluster, aflPort, curFaultFile, monitor, testID, waitSeconds);
		this.controllerPort = controllerPort;
		this.maxDownGroup = maxDownGroup;
	}

	

	@Override
	public void doTarget() {
		replayATest(mSeqPair, controllerPort, curFaultFile, maxDownGroup, aflPort, mTestID, mWaitSeconds);
	}

	@Override
	public ReplayResult afterTarget() {
		sendNotReplayToCluster(controllerResult.finalCluster);
		// For replay target, we should collect run-time information for all scenarios.
		String runInfoPath = collectRuntimeInfo(controllerResult.finalCluster);
		boolean findBug = checkIfABugExist(runInfoPath);
		mResult = generateReplayResult(finishWorkload, controllerResult.allPointsAreReplayed , findBug);
		return mResult;
	}

	/*
	 * Before we collect information from cluster, we should ask the cluster to exit replay mode first.
	 */
	protected void sendNotReplayToCluster(List<MaxDownNodes> cluster) {
		Stat.log("Command to wait all nodes not replay ...");
		AflCli.executeCliCommandToCluster(cluster, aflPort, AflCommand.NOTREPLAY, 300000);
		// executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.NOTREPLAY, 300000);
		Stat.log("Finish waiting all nodes not replay ...");
	}

    private int replayATest(QueueEntryRuntime entryRuntime, int controllerPort, File curFaultFile, List<MaxDownNodes> maxDownGroup, int aflPort, String testID, long waitSeconds) {
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(entryRuntime.faultSeq.toString()));
		logInfo.add(Stat.log("}"));

		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		// final ReplayController dController = new ReplayController(mCluster, conf.CONTROLLER_PORT, conf);
		final ReplayController dController = new ReplayController(mCluster, controllerPort, curFaultFile, maxDownGroup, aflPort);
		// final ReplayController dController = new ReplayController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(dController.cluster.prepareCluster());
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		// dController.prepareFaultSeq(seq);
		dController.prepareFaultSeqAndIOSeq(entryRuntime);
		logInfo.add(Stat.log("Start controller ..."));
		dController.startController();
		
		logInfo.add(Stat.log("Waiting for alive controller server thread ..."));
		while(!dController.serverThread.isAlive()) {
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
				logInfo.addAll(dController.cluster.runWorkload());
				logInfo.add(Stat.log("The workload was finished."));
			}
		};
		long start = System.currentTimeMillis();
		runWorkload.start();
		
		int waitIdx = 0;
		while ((runWorkload.isAlive() || dController.finishFlag == false) && waitIdx < waitSeconds) {
			try {
                Thread.sleep(1000);
                waitIdx++;
				Stat.log("waitIdx: " + waitIdx);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		dController.stopController();
		/*
		 * Collect the result of replay controller and the result of workload
		 */
		controllerResult = dController.collectReplayResult();
		finishWorkload = !runWorkload.isAlive();
		if (!controllerResult.allPointsAreReplayed) {
			logInfo.addAll(dController.rst);
		}
		a_exec_seconds = Fuzzer.getExecSeconds(start);
		Stat.log("workload end");

		return ret;
	}

	


	private boolean checkIfABugExist(String runInfoPath) {
		boolean result = false;
		if (controllerResult.allPointsAreReplayed) {
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

	/**
	 * 0: workload finish, replay finish. replay success
	 * 1: workload finish, replay finish, and we find a bug.
	 * 2: workload finish, replay not finish. replay failed
	 * 3: workload not finish, replay finish. not possible
	 * -1: workload not finish, replay not finish. we don't know whether the replay
	 * could success since the time is not enough.
	 * 
	 * It is not accurate since the workload execution is relative to replay
	 * execution. However, since we don't instrument
	 * cilent side, we couldn't put them together.
	 */
	public ReplayResult generateReplayResult(boolean workloadFinish, boolean replayFinish, boolean findBug) {
		ReplayResult result = new ReplayResult();
		if (workloadFinish && replayFinish && findBug) {
			result.result = 0;
		} else if (workloadFinish && replayFinish && !findBug) {
			result.result = 1;
		} else if (workloadFinish && !replayFinish) {
			result.result = 2;
		} else if (!workloadFinish && replayFinish) {
			result.result = 3;
		} else if (!workloadFinish && !replayFinish) {
			result.result = -1;
		}

		// construct result.info with logInfo
		result.info = "";
		for (String s : logInfo) {
			result.info += s + "\n";
		}
		return result;
	}


	public void compareRecordAndIOList(List<RunTimeIOPoint> record, List<IOPoint> ioSeq) {
        List<IOPoint> iList = ioSeq;
        int meaningSize = record.size() < iList.size() ? record.size() : iList.size();
        Stat.log("check where the differ of record and IOList begin: ");
        for (int i=0; i< meaningSize; i++) {
            RunTimeIOPoint fpb = record.get(i);
            IOPoint p = iList.get(i);
            boolean f = ReplayController.checkReportInformationEqualToIOPoint(fpb, p);
            if (!f) {
                Stat.log("record and IOList differ from index: " + i);
            }
        }
        Stat.log("check where the differ of record and IOList end.");
        Stat.log("check whether there is a different msgId ");
    }

	
	
}
