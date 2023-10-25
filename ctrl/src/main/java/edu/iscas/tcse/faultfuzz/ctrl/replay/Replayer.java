package edu.iscas.tcse.faultfuzz.ctrl.replay;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.EntryConstructor;
import edu.iscas.tcse.faultfuzz.ctrl.Fuzzer;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayTarget;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class Replayer {

	public Conf conf;

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


	public QueueEntry retriveReplayQueueEntryFromRSTFolder(String filepath) {
		EntryConstructor fsc = new EntryConstructor();
		List<IOPoint> ioPoints = fsc.constructIOPointList(filepath + "/" + FileUtil.ioTracesDir);
		QueueEntry e = new QueueEntry();
		e.ioSeq = ioPoints;
		FaultSequence faultSeq = FileUtil.loadcurrentFaultPoint(filepath + "/zk363curCrash");
		if (faultSeq == null) {
			faultSeq = new FaultSequence();
		}
		e.faultSeq = faultSeq;

		//TODO: Need to consider whether the workload information should be stored in the RST folder
		e.workload = Conf.currentWorkload;

		return e;
	}

	public void replay(QueueEntry entry) {
		ReplayTarget rt = new ReplayTarget();
		QueueEntryRuntime entryRuntime = new QueueEntryRuntime(entry);
		// rt.beforeTarget(seqPair, conf, "replay", conf.REPLAY_HANG_TIME);
		rt.beforeTarget(entryRuntime, conf, "replay", conf.hangSeconds);
		rt.doTarget();
		int result = rt.afterTarget().result;
		Stat.log("replay result: " + result);
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

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length < 1) {
			System.out.println("Please specify the configuration file!");
			return;
		}
		
		File confFile = new File(args[0]);
		if(!confFile.exists()) {
			System.out.println("The configuration file does not exist!");
			return;
		}

		Conf conf = new Conf(confFile);
		// conf.CONTROLLER_PORT = Integer.parseInt(args[0].trim());
		conf.loadConfigurationAndCheckAndPrint();
		
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //System.out.println(runtimeMXBean.getName());
        int myproc = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
		FileUtils.writeByteArrayToFile(new File(FileUtil.root+FileUtil.fuzzer_id_file), String.valueOf(myproc).getBytes());

		Fuzzer fuzzer = new Fuzzer(conf);
		fuzzer.start();
	}
}
