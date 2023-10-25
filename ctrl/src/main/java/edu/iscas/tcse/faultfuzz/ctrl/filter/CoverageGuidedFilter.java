package edu.iscas.tcse.faultfuzz.ctrl.filter;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;

public class CoverageGuidedFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		boolean result = false;
		/*
		 * If q.faultSeq is empty, it means that the queue entry is the frist run of a new workload.
		 * And we think it is interesting.
		 */
		if (q.faultSeq.isEmpty()) return true;
		if (faultMode != 0) return false;
		if (novalBasicBlock>0) return true;
		result = q.faultSeq.seq.get(q.faultSeq.seq.size()-1).type == FaultType.CRASH;
		if (result == true) {
			return result;
		}
		result = q.faultSeq.seq.get(q.faultSeq.seq.size()-1).type == FaultType.NETWORK_DISCONNECTION;
		return result;
	}
}