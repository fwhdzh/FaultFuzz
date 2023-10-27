package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;

public class FuzzInfo {
	public static int reportWindow = 30; //30 minutes
	
    public static long startTime = System.currentTimeMillis();
    public static long last_used_seconds = 0;

    public static long total_execs = 0;
    public static long exec_us = 0;   
    
    public static int total_skipped = 0; //including not triggered ones
    public static int total_nontrigger = 0;
    public static int total_bugs = 0;
    public static int total_hangs = 0;
    
    public static Set<Integer> testedUniqueCases = new HashSet<Integer>();
    
    public static long total_bitmap_size = 0;         /* Total bit count for all bitmaps  */
    public static long total_bitmap_entries = 0;      /* Number of bitmaps counted        */
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public static HashMap<Integer,Integer> timeToTotalCovs = new HashMap<Integer,Integer>();
    public static long lastNewCovTime = 0;
    public static int lastNewCovFaults = 0;
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToNewCovTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsBugsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public static long lastNewBugTime = 0;
    public static int lastNewBugFaults = 0;
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsHangsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public static long lastNewHangTime = 0;
    public static int lastNewHangFaults = 0;

	public static int pauseSecond = 0;
    
    public static long getUsedSeconds() {
    	// return last_used_seconds + (((System.currentTimeMillis()-startTime)/ 1000));
		return last_used_seconds + (((System.currentTimeMillis()-startTime)/ 1000)) - pauseSecond;
    }

    public static int getTotalCoverage(byte[] bytes) {
    	int finds= 0;
		for(int i = 0; i< bytes.length; i++) {
			if(bytes[i]>0) {
				finds++;
			}
		}
		return finds;
    }

	public static String toJSONString() {
		String result = "transform FuzzInfo to JSONString fail";
		// FuzzInfo fuzzInfo = new FuzzInfo();
		FuzzInfoRecord record = new FuzzInfoRecord();
		result = JSONObject.toJSONString(record);
		return result;
	}

    public static void updateTimeToFaulsHangsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToHangs = timeToFaulsHangsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToHangs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToHangs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsToTestsNum(QueueEntry q) {
    	FuzzInfo.updateTimeToFaulsToTestsNum(q.faultSeq);
    }

    public static void updateTimeToFaulsToTestsNum(FaultSequence fs) {
    	HashMap<Integer, Integer> faultsToTests = timeToFaulsToTestsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToTests.computeIfAbsent(fs.seq.size(), key -> 0);
    	faultsToTests.computeIfPresent(fs.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsToNewCovTestsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToNewCovTests = timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToNewCovTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToNewCovTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsBugsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToBugs = timeToFaulsBugsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToBugs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToBugs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }
}
