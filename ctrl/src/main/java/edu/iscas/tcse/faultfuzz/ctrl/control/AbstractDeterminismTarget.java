package edu.iscas.tcse.faultfuzz.ctrl.control;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Monitor;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public abstract class AbstractDeterminismTarget extends AbstractTarget{

    public ArrayList<String> logInfo;
	public ArrayList<String> checkInfo;
	public long a_exec_seconds;

	protected Cluster mCluster;

	// protected QueueEntry mEntry;
    protected QueueEntryRuntime mSeqPair;
	protected Conf mConf;
	protected String mTestID;
	protected long mWaitSeconds;

    @Override
    protected void beforeTarget(Object data, Conf conf, Object... args) {
		// TODO Auto-generated method stub
		mSeqPair = (QueueEntryRuntime) data;
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

	public void beforeTarget(QueueEntryRuntime seqPair, Conf conf, String testID, long waitSeconds) {
		mSeqPair = seqPair;
		mConf = conf;
		mTestID = testID;
		mWaitSeconds = waitSeconds;

		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;

		mCluster = new Cluster(conf);
	}


    
    

	
	public String collectRuntimeInfo(List<MaxDownNodes> cluster) {
		String result = "";
		logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
		AflCli.executeCliCommandToCluster(cluster, mConf, AflCommand.STABLE, 300000);
		logInfo.add(Stat.log("Finish waiting recovery processes."));
		logInfo.add(Stat.log("Command to save run-time traces ..."));
		AflCli.executeCliCommandToCluster(cluster, mConf, AflCommand.SAVE, 600000);
		logInfo.add(Stat.log("Finish saving run-time traces."));
		Monitor m = new Monitor(mConf);
		String runInfoPath = m.getTmpReportDir(mTestID);
		logInfo.add(Stat.log("Collecting run-time information ..."));
		m.collectRunTimeInfo(runInfoPath);
		Stat.debug(AbstractDeterminismTarget.class, "copy " + mConf.CUR_FAULT_FILE.getAbsolutePath() + " to " + runInfoPath);
		FileUtil.copyFileToDir(mConf.CUR_FAULT_FILE.getAbsolutePath(), runInfoPath);
		result = runInfoPath;
		return result;
	}

    public int checkBug(FaultSequence seq, Conf conf) {
		// TODO Auto-generated method stub
		boolean phosError = false;
		for (int i = 0; i < logInfo.size(); i++) {
			String s = logInfo.get(i);
			if (s.contains("ClassCircularityError")) {
				phosError = true;
			}
			if (s.contains("FAV test has failed")) {
				// if (s.contains("ClassCircularityError")) {
				// 	phosError = true;
				// }
				this.checkInfo.add(s);
			}
		}
		return phosError ? -1 : ((this.checkInfo.size() > 0) ? 1 : 0);
		// int result = 0;
		// for (int i = 0 ; i < logInfo.size(); i++) {
		// 	String s = logInfo.get(i);
		// 	if (s.contains("FAV test has failed")) {
		// 						if (s.contains("ClassCircularityError")) {
		// 			phosError = true;
		// 		}
		// 	}
		// }
	}
}
