package edu.iscas.CCrashFuzzer.filter;

import edu.iscas.CCrashFuzzer.QueueEntry;

public class CrashFuzzerMinusFilter {
	public static boolean checkIfInteresting(int faultMode, int novalBasicBlock, QueueEntry q) {
		if (faultMode != 0) return false;
		return true;
	}
}