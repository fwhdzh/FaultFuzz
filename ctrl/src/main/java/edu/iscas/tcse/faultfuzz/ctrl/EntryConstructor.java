package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class EntryConstructor {

	public static class FaultListConstructionResult {
		int max_match_fault = 0;
		int candidate_io = 0;
		List<FaultPoint> faultList;
	}

	private List<IOPoint> collectIOPoints(String filePath) {
        List<IOPoint> result;
		TraceReader reader = new TraceReader(filePath);
		reader.readTraces();
		result = reader.ioPoints;
        return result;
    }

	public List<IOPoint> constructIOPointList(String filePath) {
        List<IOPoint> ioPoints = collectIOPoints(filePath);
		List<IOPoint> result = constructIOPointList(ioPoints);
		return result;
	}

	public List<IOPoint> constructIOPointListWithFname(String fname) {
		String filePath = FileUtil.root_tmp + fname + "/" + FileUtil.ioTracesDir;
		List<IOPoint> result = constructIOPointList(filePath);
		return result;
	}

	public static void sortIOPointList(List<IOPoint> ioPoints) {
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

	public static void computeAppearIdx(List<IOPoint> ioPoints) {
    	ConcurrentHashMap<Integer, AtomicInteger> uniqueEntryToAppearIdx = new ConcurrentHashMap<Integer, AtomicInteger>();
    	for(IOPoint sortedRec: ioPoints) {
    		AtomicInteger appearIdx = uniqueEntryToAppearIdx.computeIfAbsent(sortedRec.computeIoID(), k -> new AtomicInteger(0));
    		sortedRec.appearIdx = appearIdx.incrementAndGet();
        }
    }

    public List<IOPoint> constructIOPointList(List<IOPoint> ioPoints) {
		List<IOPoint> result = new ArrayList<>(ioPoints);
		EntryConstructor.sortIOPointList(result);
		EntryConstructor.fixWROrderInSameTimeStamp(result);
		EntryConstructor.computeAppearIdx(result);
		return result;
	}

    private void logIoPointList(List<IOPoint> ioPoints) {
        Stat.debug(this.getClass(), "logIoPointList");
        for (IOPoint p: ioPoints) {
            Stat.debug(this.getClass(), JSONObject.toJSONString(p));
        }
    }

	public EntryConstructor.FaultListConstructionResult mapIOPointToFaultList(List<IOPoint> iseq ,List<FaultPoint> fseq){
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
		EntryConstructor.FaultListConstructionResult result = new FaultListConstructionResult();
		result.max_match_fault = m;
		result.candidate_io = i;
		result.faultList = fseq;
		return result;
	}

	/**
	 * Construct a QueueEntry instance from IO points trace and injected faults.
	 * 
	 * @param q The queue entry to be constructed. Note that the instance of q will be modified.
	 * @param ioPoints The io points to be used to construct the queue entry. Got from the trace file.
	 * @param injectedFaultPointList The fault points to be injected into the queue entry. Got from the controller result.
	 * @return The constructed queue entry. The modified q will be returned straightly.
	 */
	public QueueEntry constructQueueEntry(QueueEntry q, List<IOPoint> ioPoints, List<FaultPoint> injectedFaultPointList) {
		if (ioPoints == null || ioPoints.isEmpty()) {
			q.ioSeq = new ArrayList<>();
		}
        List<IOPoint> constructedIOPoints = constructIOPointList(ioPoints);
        Stat.debug(this.getClass(), "constructedIOPoints: " + constructedIOPoints.size() + JSONObject.toJSONString(injectedFaultPointList));
		q.ioSeq = constructedIOPoints;
		if (q.faultSeq == null || q.faultSeq.isEmpty()) {
			q.faultSeq = new FaultSequence();
		}
        // logIoPointList(constructedIOPoints);
		EntryConstructor.FaultListConstructionResult fResult = mapIOPointToFaultList(constructedIOPoints, injectedFaultPointList);
		q.max_match_fault = fResult.max_match_fault;
		q.candidate_io = fResult.candidate_io;
		q.faultSeq.seq = fResult.faultList;
		q.faultSeq.reset();
		q.workload = Conf.currentWorkload;
        Stat.debug(this.getClass(), "The size of IO points is: " + q.ioSeq.size());
        Stat.debug(this.getClass(), "The size of faultSeq points is: " + q.faultSeq.seq.size());
		// q.calibrate();
		return q;
	}

	

	
}