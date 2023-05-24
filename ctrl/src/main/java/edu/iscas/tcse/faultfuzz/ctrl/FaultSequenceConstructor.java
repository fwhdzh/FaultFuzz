package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class FaultSequenceConstructor {

	private List<IOPoint> collectIOPoints(String fname) {
        List<IOPoint> result;
		TraceReader reader = new TraceReader(FileUtil.root_tmp + fname + "/" + FileUtil.ioTracesDir);
		reader.readTraces();
		result = reader.ioPoints;
        return result;
    }

	public List<IOPoint> constructIOPointList(String fname) {
        List<IOPoint> ioPoints = collectIOPoints(fname);
		List<IOPoint> result = constructIOPointList(ioPoints);
		return result;
	}

    public List<IOPoint> constructIOPointList(List<IOPoint> ioPoints) {
		List<IOPoint> result = new ArrayList<>(ioPoints);
		FaultSequenceConstructor.sortIOPointList(result);
		FaultSequenceConstructor.fixWROrderInSameTimeStamp(result);
		FaultSequenceConstructor.computeAppearIdx(result);
		return result;
	}

	public void updateQWithTrace(QueueEntry q, String fname) {
		// after test, the retrieved ioSeq could be different from the original q.ioSeq
		// the actual faultSeq could also be different from the original q.faultSeq
	
		// read from file,add to queue
		// TraceReader reader = new TraceReader(FileUtil.root_tmp + fname + "/" + FileUtil.ioTracesDir);
		// reader.readTraces();
		// List<IOPoint> ioPoints = reader.ioPoints;

		List<IOPoint> ioPoints = constructIOPointList(fname);
		if (ioPoints == null || ioPoints.isEmpty()) {
			return;
		}
		q.ioSeq = ioPoints;
		q.calibrate();
	}

    private void logIoPointList(List<IOPoint> ioPoints) {
        Stat.debug(this.getClass(), "logIoPointList");
        for (IOPoint p: ioPoints) {
            Stat.debug(this.getClass(), JSONObject.toJSONString(p));
        }
    }

	public QueueEntry constructQueueEntry(QueueEntry q, List<IOPoint> ioPoints, List<FaultPoint> injectedFaultPointList) {
		if (ioPoints == null || ioPoints.isEmpty()) {
			q.ioSeq = new ArrayList<>();
		}
        List<IOPoint> constructedIOPoints = constructIOPointList(ioPoints);
        Stat.debug(this.getClass(), "constructedIOPoints: " + constructedIOPoints.size() + JSONObject.toJSONString(injectedFaultPointList));
		q.ioSeq = ioPoints;
		if (q.faultSeq == null || q.faultSeq.isEmpty()) {
			q.faultSeq = new FaultSequence();
		}
        logIoPointList(constructedIOPoints);
		FaultSequenceConstructor.FaultListConstructionResult fResult = mapIOPointToFaultList(constructedIOPoints, injectedFaultPointList);
		q.max_match_fault = fResult.max_match_fault;
		q.candidate_io = fResult.candidate_io;
		q.faultSeq.seq = fResult.faultList;
		q.faultSeq.reset();
        Stat.debug(this.getClass(), "The size of IO points is: " + q.ioSeq.size());
        Stat.debug(this.getClass(), "The size of faultSeq points is: " + q.faultSeq.seq.size());
		// q.calibrate();
		return q;
	}

	public static class FaultListConstructionResult {
		int max_match_fault = 0;
		int candidate_io = 0;
		List<FaultPoint> faultList;
	}

	public FaultSequenceConstructor.FaultListConstructionResult mapIOPointToFaultList(List<IOPoint> iseq ,List<FaultPoint> fseq){
		int i = 0;
		int m = 0;
		while ((i < iseq.size()) && m < fseq.size()) {
			if (iseq.get(i).CALLSTACK.toString().equals(fseq.get(m).ioPt.CALLSTACK.toString())
					&& iseq.get(i).appearIdx == fseq.get(m).ioPt.appearIdx) {
				fseq.get(m).ioPt = iseq.get(i);
				fseq.get(m).tarNodeIp = fseq.get(m).actualNodeIp;
				fseq.get(m).actualNodeIp = null;
				/**
				 * Wenhan Feng
				 * We should add an "actualParams" field if we still want to use global appearId to determine whether a I/O point
				 * should be injected a fault
				 */
				fseq.get(m).params = fseq.get(m).params;
				m++;
			}
			i++;
		}
		FaultSequenceConstructor.FaultListConstructionResult result = new FaultListConstructionResult();
		result.max_match_fault = m;
		result.candidate_io = i;
		result.faultList = fseq;
		return result;
	}

	

	public void updateQWithTraceBackup(QueueEntry q, String fname) {
		// after test, the retrieved ioSeq could be different from the original q.ioSeq
		// the actual faultSeq could also be different from the original q.faultSeq
	
		// read from file,add to queue
		// TraceReader reader = new TraceReader(FileUtil.root_tmp + fname + "/" + FileUtil.ioTracesDir);
		// reader.readTraces();
		// List<IOPoint> ioPoints = reader.ioPoints;

		List<IOPoint> ioPoints = constructIOPointList(fname);
		if (ioPoints == null || ioPoints.isEmpty()) {
			return;
		}
		q.ioSeq = ioPoints;
		q.calibrate();
	}

    public static void sortIOPointList(List<IOPoint> ioPoints) {
		// ioPoints.sort(Comparator.comparingLong(a -> a.TIMESTAMP));
	
		ioPoints.sort(new Comparator<IOPoint>() {
			@Override
			public int compare(IOPoint o1, IOPoint o2) {
				// TODO Auto-generated method stub
				if (o1.TIMESTAMP < o2.TIMESTAMP) {
					return -1;
				}
				if (o1.TIMESTAMP > o2.TIMESTAMP) {
					return 1;
				}
				int compareIPResult = o1.ip.compareTo(o2.ip);
				return compareIPResult;
			}
			
		});
	}

	public static void computeAppearIdx(List<IOPoint> ioPoints) {
    	ConcurrentHashMap<Integer, AtomicInteger> uniqueEntryToAppearIdx = new ConcurrentHashMap<Integer, AtomicInteger>();
    	for(IOPoint sortedRec: ioPoints) {
    		AtomicInteger appearIdx = uniqueEntryToAppearIdx.computeIfAbsent(sortedRec.computeIoID(), k -> new AtomicInteger(0));
    		sortedRec.appearIdx = appearIdx.incrementAndGet();
        }
    }

    public static void fixWROrderInSameTimeStamp(List<IOPoint> ioPoints) {
		for (int i = 0; i < ioPoints.size(); i++) {
			IOPoint p = ioPoints.get(i);
			String path = p.PATH;
			if (path.contains("FAVMSG:READ")) {
				String msgId = path.split("&")[1];
				for (int j = (i+1); j < ioPoints.size(); j++) {
					IOPoint wp = ioPoints.get(j);
					if (wp.TIMESTAMP > p.TIMESTAMP) {
						break;
					}
					if (wp.PATH.contains("FAVMSG") && wp.PATH.endsWith(msgId)) {
						Stat.log("transform index with " + i + ": [" + p.PATH + "], and " + j + ": [" + wp.PATH + "]");
						ioPoints.set(i, wp);
						ioPoints.set(j, p);
						break;
					}
				} 
			}
		}
	}

}