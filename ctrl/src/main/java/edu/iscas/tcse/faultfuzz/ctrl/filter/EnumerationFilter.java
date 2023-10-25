package edu.iscas.tcse.faultfuzz.ctrl.filter;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;

public class EnumerationFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		if (faultMode != 0) return false;
		return true;
	}
}