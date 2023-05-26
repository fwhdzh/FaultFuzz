package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.alibaba.fastjson.JSON;

import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractDeterminismTarget.FaultSeqAndIOSeq;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayTarget;
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
		// TraceReader tr = new TraceReader(filepath + "/fav-rst");
        // tr.readTraces();
		// tr.fixWROrderInSameTimeStamp(tr.ioPoints);
		FaultSequenceConstructor fsc = new FaultSequenceConstructor();
		List<IOPoint> ioPoints = fsc.constructIOPointList(filepath + "/" + FileUtil.ioTracesDir);
        QueueEntry e = new QueueEntry();
        e.ioSeq = ioPoints;
        FaultSequence faultSeq = FileUtil.loadcurrentFaultPoint(filepath + "/zk363curCrash");
		if (faultSeq == null) {
			faultSeq = new FaultSequence();
			// faultSeq.seq.add(null);
		}
		e.faultSeq = faultSeq;
		return e;
	}

	
	public void replay(QueueEntry entry) {
		ReplayTarget rt = new ReplayTarget();
		FaultSeqAndIOSeq seqPair = new FaultSeqAndIOSeq(entry.faultSeq, entry.ioSeq);
		// rt.beforeTarget(seqPair, conf, "replay", conf.REPLAY_HANG_TIME);
		rt.beforeTarget(seqPair, conf, "replay", conf.hangSeconds);
		rt.doTarget();
		int result = rt.afterTarget().result;
		Stat.log("replay result: " + result);
	}
}
