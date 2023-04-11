package edu.iscas.CCrashFuzzer.control;

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
import edu.iscas.CCrashFuzzer.Controller.AbortFaultException;
import edu.iscas.CCrashFuzzer.control.ReplayController.FaultPointBlocked;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class ReplayTarget extends AbstractTarget{
    public ArrayList<String> logInfo;
	public ArrayList<String> checkInfo;
	public long a_exec_seconds;

	QueueEntry mEntry;
	Conf mConf;
	String mTestID;
	long mWaitSeconds;

	int mResult;

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
	}

	@Override
	public void doTarget() {
		// TODO Auto-generated method stub
		mResult = replayATest(mEntry, mConf, mTestID, mWaitSeconds);
	}

	@Override
	public ReplayResult afterTarget() {
		// TODO Auto-generated method stub
		ReplayResult replayRst = new ReplayResult();
		replayRst.result = mResult;
		return replayRst;
	}

    private int replayATest(QueueEntry entry, final Conf conf, String testID, long waitSeconds) {

        logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;
		
		logInfo.add(Stat.log("=========================Going to conduct test "+testID+"("+waitSeconds+"s)========================="));
		logInfo.add(Stat.log(""));
		logInfo.add(Stat.log("Fault sequence info {"));
		logInfo.add(Stat.log(entry.faultSeq.toString()));
		logInfo.add(Stat.log("}"));

		int ret = 0;
		//prepare the cluster, e.g., format the namenode of HDFS. could be do nothing
		//prepare current crash point and corresponding crash event, i.e., crash
		//or remote crash
		final ReplayController dController = new ReplayController(new Cluster(conf), conf.CONTROLLER_PORT, conf);
//        controller.prepareFaultSeq(FaultSequence.getEmptyIns());//keep curCrash null
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
		boolean addedController = false;
		do {
			if(dController.injectionAborted) {
				ret = -1;
				addedController = true;
				logInfo.addAll(dController.rst);
		    	logInfo.add(Stat.log("Exit abnormally since current fault sequence was aborted, stop controller ..."));
        		break;
			}
		    try {
                Thread.sleep(1000);
                waitIdx++;

				Stat.log("waitIdx: " + waitIdx);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		    if(waitIdx > waitSeconds) {
				if (!dController.arriveAllFaultPoint) {
					logInfo.add("replay failed");
				} else {
					logInfo.add("replay success");
					// logInfo.add(Stat.log("Command to ask all nodes to continue in a unreplay(normal) mode..."));
					// executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.NOTREPLAY, 300000);
					// logInfo.add(Stat.log("Continue all the FPB in list ..."));
					// try {
					// 	dController.continueAllFPB();
					// } catch (IOException | AflException | AbortFaultException e) {
					// 	// TODO Auto-generated catch block
					// 	e.printStackTrace();
					// }
				}
				try {
					dController.serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
            	// if(!dController.faultInjected) {
            	// 	logInfo.addAll(dController.rst);
    		    // 	logInfo.add(Stat.log("Exit abnormally after waiting "+waitSeconds+" seconds, stop controller ..."));
    		    // 	addedController = true;
    		    // 	ret = -1;
    		    // 	break;
            	// } else if (dController.faultInjected && runWorkload.isAlive()) {
            	// 	logInfo.addAll(dController.rst);
            	// 	logInfo.add(Stat.log("FAV test has failed: the run did not finished in "+waitSeconds+" seconds."));
            	// 	ret = 2;
            	// 	addedController = true;
            	// 	break;
            	// }
            }
		} while(!dController.faultInjected || runWorkload.isAlive());

		Stat.log("workload end");
		Stat.log("Command to wait all nodes not replay ...");
		executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.NOTREPLAY, 300000);
		Stat.log("Finish waiting all nodes not replay ...");

		a_exec_seconds = Fuzzer.getExecSeconds(start);
		
		if(!addedController) {
			logInfo.addAll(dController.rst);
		}

		List<FaultPointBlocked> arriveFPBList = dController.arriveFPBList;

		List<FaultPointBlocked> actualFPBList = dController.actualFPBList;
		compareRecordAndEntry(actualFPBList, entry);
		
		FaultPointBlocked.recordFPBList(actualFPBList, conf.REPLAY_ACTUAL_FPB_LIST_PATH);

		if(ret != -1) {// not need to wait not triggered cases
			//wait recovery process finish
			logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
			executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.STABLE, 300000);
			logInfo.add(Stat.log("Finish waiting recovery processes."));
			logInfo.add(Stat.log("Command to save run-time traces ..."));
			executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.SAVE, 600000);
			logInfo.add(Stat.log("Finish saving run-time traces."));
		}

		Monitor m = new Monitor(conf);
		String runInfoPath = m.getTmpReportDir(testID);
		if(ret != -1) {//no need to collect traces and logs for not triggered ones
			logInfo.add(Stat.log("Collecting run-time information ..."));
			m.collectRunTimeInfo(runInfoPath);
			FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), runInfoPath);
		}
		
		FaultSequence seq = entry.faultSeq;

		if(ret == 0) {
			logInfo.add(Stat.log("Going to check the system. Faults injected: "+seq.toString()));
			// replay target doesn't need to check
			// logInfo.addAll(dController.cluster.runChecker(conf, dController.currentCluster, runInfoPath+FileUtil.monitorDir));
			// ret = checkBug(seq, conf);
			logInfo.add(Stat.log("Exit normally, stop controller ..."));
		}

		// RunCommand.run("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/test.sh");
		dController.stopController();
		// RunCommand.run("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/test.sh");
		return ret;
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
