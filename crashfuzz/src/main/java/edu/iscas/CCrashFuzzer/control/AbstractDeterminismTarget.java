package edu.iscas.CCrashFuzzer.control;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.CCrashFuzzer.AflCli;
import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.IOPoint;
import edu.iscas.CCrashFuzzer.Monitor;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public abstract class AbstractDeterminismTarget extends AbstractTarget{

    public static class FaultSeqAndIOSeq {
        public FaultSequence faultSeq;
        public List<IOPoint> ioSeq;

        public FaultSeqAndIOSeq(FaultSequence faultSeq, List<IOPoint> ioSeq) {
            this.faultSeq = faultSeq;
            this.ioSeq = ioSeq;
        }
    }
    
    public ArrayList<String> logInfo;
	public ArrayList<String> checkInfo;
	public long a_exec_seconds;

	protected Cluster mCluster;

	// protected QueueEntry mEntry;
    protected FaultSeqAndIOSeq mSeqPair;
	protected Conf mConf;
	protected String mTestID;
	protected long mWaitSeconds;

    @Override
    public void beforeTarget(Object data, Conf conf, Object... args) {
		// TODO Auto-generated method stub
		mSeqPair = (FaultSeqAndIOSeq) data;
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

    /*
	 * Before we collect information from cluster, we should ask the cluster to exit replay mode first.
	 */
	protected void sendNotReplayToCluster(List<MaxDownNodes> cluster) {
		Stat.log("Command to wait all nodes not replay ...");
		executeCliCommandToCluster(cluster, mConf, AflCommand.NOTREPLAY, 300000);
		// executeCliCommandToCluster(dController.currentCluster, conf, AflCommand.NOTREPLAY, 300000);
		Stat.log("Finish waiting all nodes not replay ...");
	}

	
	public String collectRuntimeInfo(List<MaxDownNodes> cluster) {
		String result = "";
		logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
		executeCliCommandToCluster(cluster, mConf, AflCommand.STABLE, 300000);
		logInfo.add(Stat.log("Finish waiting recovery processes."));
		logInfo.add(Stat.log("Command to save run-time traces ..."));
		executeCliCommandToCluster(cluster, mConf, AflCommand.SAVE, 600000);
		logInfo.add(Stat.log("Finish saving run-time traces."));
		Monitor m = new Monitor(mConf);
		String runInfoPath = m.getTmpReportDir(mTestID);
		logInfo.add(Stat.log("Collecting run-time information ..."));
		m.collectRunTimeInfo(runInfoPath);
		FileUtil.copyFileToDir(mConf.CUR_CRASH_FILE.getAbsolutePath(), runInfoPath);
		result = runInfoPath;
		return result;
	}

    public int checkBug(FaultSequence seq, Conf conf) {
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
    
}
