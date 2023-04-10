package edu.iscas.tcse.favtrigger.triggering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPos;
// import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultStat;
// import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPoint;

import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.utils.FileUtil;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.IOPoint;


public class CurrentFaultSequence {
	public static FaultSequence faultSeq;
    public static void loadCurrentCrashPoint(String cur_crash_path) {
		faultSeq = FileUtil.loadCurrentCrashPoint(cur_crash_path);
    	
    }
}
