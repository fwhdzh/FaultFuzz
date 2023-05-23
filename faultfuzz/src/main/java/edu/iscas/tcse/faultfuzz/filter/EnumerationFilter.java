package edu.iscas.tcse.faultfuzz.filter;

import edu.iscas.tcse.faultfuzz.QueueEntry;

public class EnumerationFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		if (faultMode != 0) return false;
		return true;
	}
}