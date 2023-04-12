package edu.iscas.CCrashFuzzer.control.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import edu.iscas.CCrashFuzzer.AflCli;
import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.Fuzzer;
import edu.iscas.CCrashFuzzer.IOPoint;
import edu.iscas.CCrashFuzzer.Monitor;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.control.AbstractTarget;
import edu.iscas.CCrashFuzzer.control.NormalController.AbortFaultException;
import edu.iscas.CCrashFuzzer.control.replay.ReplayController.FaultPointBlocked;
import edu.iscas.CCrashFuzzer.control.replay.ReplayController.ReplayControllerResult;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class ReplayTarget extends AbstractTarget{

    public ArrayList<String> logInfo;
	public ArrayList<String> checkInfo;
	public long a_exec_seconds;

	private Cluster mCluster;

	private QueueEntry mEntry;
	private Conf mConf;
	private String mTestID;
	private long mWaitSeconds;

	// variables conncecting doTarget and afterTarget 
	private ReplayControllerResult controllerResult;
	private boolean finishWorkload;

	// target result
	private ReplayResult mResult;

	public static class ReplayResult {
		public int result;
	}

	@Override
	public void beforeTarget(Object data, Conf conf, Object... args) {
		// TODO Auto-generated method stub
		mEntry = (QueueEntry) data;
		mConf = conf;
		if (args.length == 2) {
			mTestID = (String) args[0];
			mWaitSeconds = (Long) args[1];
		}
		else {
			throw new IllegalArgumentException("replay target args should be 2");
		}

		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;

		mCluster = new Cluster(conf);
	}

	@Override
	public void doTarget() {
		replayATest(mEntry, mConf, mTestID, mWaitSeconds);
	}

	@Override
	public ReplayResult afterTarget() {
		sendNotReplayToCluster();
		// For replay target, we should collect run-time information for all scenarios.
		String runInfoPath = collectRuntimeInfo();
		boolean findBug = checkIfABugExist(runInfoPath);
		mResult = generateReplayResult(finishWorkload, controllerResult.allPointsAreReplayed , findBug);
		return mResult;
	}

    private int replayATest(QueueEntry entry, final Conf conf, String testID, long waitSeconds) {
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(entry.faultSeq.toString()));
		logInfo.add(Stat.log("}"));

		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		final ReplayController dController = new ReplayController(mCluster, conf.CONTROLLER_PORT, conf);
		// final ReplayController dController = new ReplayController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
		logInfo.add(Stat.log("Prepare cluster ..."));
		logInfo.addAll(dController.cluster.prepareCluster());
		logInfo.add(Stat.log("Prepare current fault sequence ..."));
		// dController.prepareFaultSeq(seq);
		dController.prepareQueueEntry(entry);
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

	/*
	 * Before we collect information from cluster, we should ask the cluster to exit replay mode first.
	 */
	private void sendNotReplayToCluster() {
		Stat.log("Command to wait all nodes not replay ...");
		executeCliCommandToCluster(controllerResult.finalCluster, mConf, AflCommand.NOTREPLAY, 300000);
		// executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.NOTREPLAY, 300000);
		Stat.log("Finish waiting all nodes not replay ...");
	}

	
	private String collectRuntimeInfo() {
		String result = "";
		logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
		executeCliCommandToCluster(controllerResult.finalCluster, mConf, AflCommand.STABLE, 300000);
		logInfo.add(Stat.log("Finish waiting recovery processes."));
		logInfo.add(Stat.log("Command to save run-time traces ..."));
		executeCliCommandToCluster(controllerResult.finalCluster, mConf, AflCommand.SAVE, 600000);
		logInfo.add(Stat.log("Finish saving run-time traces."));
		Monitor m = new Monitor(mConf);
		String runInfoPath = m.getTmpReportDir(mTestID);
		logInfo.add(Stat.log("Collecting run-time information ..."));
		m.collectRunTimeInfo(runInfoPath);
		FileUtil.copyFileToDir(mConf.CUR_CRASH_FILE.getAbsolutePath(), runInfoPath);
		result = runInfoPath;
		return result;
	}


	private boolean checkIfABugExist(String runInfoPath) {
		boolean result = false;
		if (controllerResult.allPointsAreReplayed) {
			FaultSequence seq = mEntry.faultSeq;
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
		return result;
	}

	public void executeCliCommandToCluster(List<MaxDownNodes> cluster, final Conf conf, AflCommand command, long waitTime) {
		List<Thread> workThreads = new ArrayList<Thread>();
		for (MaxDownNodes subCluster : cluster) {
			for (final String alive : subCluster.aliveGroup) {
				Thread t = new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						String[] args = new String[3];
						args[0] = alive;
						args[1] = String.valueOf(conf.AFL_PORT);
						args[2] = command.toString();
						// args[2] = AflCommand.STABLE.toString();

						logInfo.add(Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args)));

						try {
							AflCli.main(args);
						} catch (AflException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				};
				t.start();
				workThreads.add(t);
			}
		}
		for (Thread t : workThreads) {
			try {
				t.join(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int checkBug(FaultSequence seq, Conf conf) {
		// TODO Auto-generated method stub
		boolean phosError = false;
		for (int i = 0; i < logInfo.size(); i++) {
			String s = logInfo.get(i);
			if (s.contains("FAV test has failed")) {
				if (s.contains("ClassCircularityError")) {
					phosError = true;
				}
				this.checkInfo.add(s);
			}
		}
		return phosError ? -1 : ((this.checkInfo.size() > 0) ? 1 : 0);
	}


	public void compareRecordAndEntry(List<FaultPointBlocked> record, QueueEntry entry) {
        List<IOPoint> iList = entry.ioSeq;
        int meaningSize = record.size() < iList.size() ? record.size() : iList.size();
        Stat.log("check where the differ of record and IOList begin: ");
        for (int i=0; i< meaningSize; i++) {
            FaultPointBlocked fpb = record.get(i);
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
