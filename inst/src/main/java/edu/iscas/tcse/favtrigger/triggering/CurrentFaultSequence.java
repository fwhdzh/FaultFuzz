package edu.iscas.tcse.favtrigger.triggering;

import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;


public class CurrentFaultSequence {
	public static FaultSequence faultSeq;
    public static void loadCurrentCrashPoint(String cur_crash_path) {
		faultSeq = FileUtil.loadCurrentCrashPoint(cur_crash_path);
    	
    }
}
