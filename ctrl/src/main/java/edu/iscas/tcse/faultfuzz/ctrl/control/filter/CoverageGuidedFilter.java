package edu.iscas.tcse.faultfuzz.ctrl.control.filter;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultStat;

public class CoverageGuidedFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		boolean result = false;
		if (faultMode != 0) return false;
		if (novalBasicBlock>0) return true;
		result = q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH;
		if (result == true) {
			return result;
		}
		result = q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.NETWORK_DISCONNECT;
		// result = (faultMode == 0) && (nb>0 || q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH);
		return result;
	}
}