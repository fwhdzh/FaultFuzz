package edu.iscas.tcse.faultfuzz.ctrl.replay;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;

import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayTarget.ReplayResult;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class Replayer {

	public Conf conf;

	public ReplayConf replayConf;

	public Replayer(Conf conf) {
		this.conf = conf;
	}

	public QueueEntry retriveReplayQueueEntryFromJSONFilePath(String filepath) {
		QueueEntry result = null;
		File file = new File(filepath);
		List<String> oriList;
		try {
			oriList = Files.readAllLines(file.toPath());
			String s = oriList.get(0);
			QueueEntry entry = JSON.parseObject(s, QueueEntry.class);
			// Stat.log(JSONObject.toJSONString(entry));
			result = entry;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public String generateReplayReport(ReplayResult replayResult) {
		String s = "";
		s = s + "*************************************************" + "\n";
		s = s + "**************Replay has finished********************" + "\n";
		s = s + "replay result: " + replayResult.result + "\n";
		String explainResultString = "";
		explainResultString = explainResultString + "Explaination of the replay result:" + "\n";
		explainResultString = explainResultString + "0: workload finish, replay finish (trigger all I/O points and faults). replay success" + "\n";
		explainResultString = explainResultString + "1: workload finish, replay finish, and we find a bug." + "\n";
		explainResultString = explainResultString + "2: workload finish, replay not finish. replay failed" + "\n";
		explainResultString = explainResultString + "3: workload not finish, replay finish. not possible" + "\n";
		explainResultString = explainResultString + "-1: workload not finish, replay not finish. we don't know whether the replay could success since the time is not enough." + "\n";
		explainResultString = explainResultString + "Replay is not always available since FaultFuzz cannot control all non-determinism of the target system. Users can check the detailed information of the test in the folder storing test data" + "\n";
		s = s + explainResultString;
		s = s + "*************************************************" + "\n";
		s = s + "The replay log is:" + "\n";
		s = s + replayResult.info + "\n";
		s = s + "**********************End************************" + "\n";
		return s;
	}


	public String replay(QueueEntry entry) {
		ReplayTarget rt = new ReplayTarget();
		QueueEntryRuntime entryRuntime = new QueueEntryRuntime(entry);
		// rt.beforeTarget(seqPair, conf, "replay", conf.REPLAY_HANG_TIME);
		Cluster cluster = new Cluster(conf);
		rt.beforeTarget(entryRuntime, cluster, conf.AFL_PORT, conf.CUR_FAULT_FILE, conf.MONITOR, "replay", conf.hangSeconds, conf.CONTROLLER_PORT, conf.maxDownGroup);
		rt.doTarget();
		ReplayResult replayResult = rt.afterTarget();
		cluster.prepareCluster(); //clean the test environment

		String report = generateReplayReport(replayResult);
		Stat.log(report);
		return report;
	}

	public boolean checkQueueEntrySuitedToReplay(QueueEntry q) {
		boolean result = true;
		if (q.faultSeq.seq.size() <= 0) {
			return false;
		}
		if (q.ioSeq.indexOf(q.faultSeq.seq.get(0).ioPt) < 1) {
			return false;
		}
		return result;
	}

	/**
	 * Main function for Replayer
	 * 
	 * @param args args[0] is the configuration file, can be normal conf format or
	 *             special replayer format.
	 *             args[1] is the path of the RST folder
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.out.println("Please specify the configuration file, the fav_rst!");
			return;
		}
		String replayReportPath = null;
		if (args.length >= 3) {
			Stat.log("replay report path is " + args[2]);
			replayReportPath = args[2];
		}

		File confFile = new File(args[0]);
		if (!confFile.exists()) {
			System.out.println("The configuration file does not exist!");
			return;
		}

		Conf conf = new Conf(confFile);
		conf.loadConfiguration();

		String rstParentFolder = args[1];

		ReplayConf rConf = new ReplayConf(conf, rstParentFolder);

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		// System.out.println(runtimeMXBean.getName());
		int myproc = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
		FileUtils.writeByteArrayToFile(new File(FileUtil.root + FileUtil.fuzzer_id_file),
				String.valueOf(myproc).getBytes());

		Replayer replayer = new Replayer(conf);
		QueueEntry entry = FileUtil.retriveReplayQueueEntryFromRSTFolder(rstParentFolder, conf.CUR_FAULT_FILE.getName());
		String replayReport = replayer.replay(entry);

		if (replayReportPath != null) {
			Stat.log("write replay report to " + replayReportPath);
			FileUtils.writeByteArrayToFile(new File(replayReportPath), replayReport.getBytes());
			Stat.log("write replay report to " + replayReportPath + " done!");
		}
		

		
	}
}
