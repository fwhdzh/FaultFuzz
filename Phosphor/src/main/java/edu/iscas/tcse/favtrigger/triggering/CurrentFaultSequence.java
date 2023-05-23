package edu.iscas.tcse.favtrigger.triggering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.iscas.tcse.faultfuzz.FaultSequence;
import edu.iscas.tcse.faultfuzz.IOPoint;
import edu.iscas.tcse.faultfuzz.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.FaultSequence.FaultPos;
import edu.iscas.tcse.faultfuzz.FaultSequence.FaultStat;
import edu.iscas.tcse.faultfuzz.utils.FileUtil;


public class CurrentFaultSequence {
	public static FaultSequence faultSeq;
    public static void loadCurrentCrashPoint(String cur_crash_path) {
		faultSeq = FileUtil.loadCurrentCrashPoint(cur_crash_path);
    	
    }
}
