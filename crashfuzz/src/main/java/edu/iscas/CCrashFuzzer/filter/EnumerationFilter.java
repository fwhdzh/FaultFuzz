package edu.iscas.CCrashFuzzer.filter;

import edu.iscas.CCrashFuzzer.QueueEntry;

public class EnumerationFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		if (faultMode != 0) return false;
		return true;
	}
}