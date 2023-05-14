package edu.iscas.CCrashFuzzer.filter;

import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;

public class CoverageFilter {
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