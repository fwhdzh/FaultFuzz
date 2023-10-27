package edu.iscas.tcse.faultfuzz.ctrl.control;

import java.io.File;
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

	protected int aflPort;
	protected File curFaultFile;
	protected File monitor;

    @Override
    protected void beforeTarget(Object data, Object... args) {
		// TODO Auto-generated method stub
		mSeqPair = (QueueEntryRuntime) data;

		// mConf = conf;
		// aflPort = conf.AFL_PORT;
		// curFaultFile = conf.CUR_FAULT_FILE;
		// monitor = conf.MONITOR;

		if (args.length == 6) {
			mCluster = (Cluster) args[0];
			aflPort = (Integer) args[1];
			curFaultFile = (File) args[2];
			monitor = (File) args[3];
			mTestID = (String) args[4];
			mWaitSeconds = (Long) args[5];
		}
		else {
			throw new IllegalArgumentException("replay target args should be 6");
		}

		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;
	}

	public void beforeTarget(QueueEntryRuntime seqPair, Cluster cluster, int aflPort, File curFaultFile, File monitor, String testID, long waitSeconds) {
		mSeqPair = seqPair;
		// mConf = conf;

		mCluster = cluster;

		this.aflPort = aflPort;
		this.curFaultFile = curFaultFile;
		this.monitor = monitor;

		mTestID = testID;
		mWaitSeconds = waitSeconds;

		logInfo = new ArrayList<String>();
		checkInfo = new ArrayList<String>();
		a_exec_seconds = 0;

		// mCluster = new Cluster(conf);
		
	}

	public String collectRuntimeInfo(List<MaxDownNodes> cluster) {
		String result = "";
		logInfo.add(Stat.log("Command to wait all recovery process complete ..."));
		AflCli.executeCliCommandToCluster(cluster, aflPort, AflCommand.STABLE, 300000);
		logInfo.add(Stat.log("Finish waiting recovery processes."));
		logInfo.add(Stat.log("Command to save run-time traces ..."));
		AflCli.executeCliCommandToCluster(cluster, aflPort, AflCommand.SAVE, 600000);
		logInfo.add(Stat.log("Finish saving run-time traces."));
		// Monitor m = new Monitor(mConf);
		Monitor m = new Monitor(monitor.getAbsolutePath());
		String runInfoPath = m.getTmpReportDir(mTestID);
		logInfo.add(Stat.log("Collecting run-time information ..."));
		m.collectRunTimeInfo(runInfoPath);
		Stat.debug(AbstractDeterminismTarget.class, "copy " + curFaultFile.getAbsolutePath() + " to " + runInfoPath);
		FileUtil.copyFileToDir(curFaultFile.getAbsolutePath(), runInfoPath);
		result = runInfoPath;
		return result;
	}

    public int checkBug(FaultSequence seq) {
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
