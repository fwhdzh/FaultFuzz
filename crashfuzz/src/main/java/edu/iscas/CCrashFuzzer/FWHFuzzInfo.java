package edu.iscas.CCrashFuzzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FWHFuzzInfo {
    public int reportWindow = 30; //30 minutes
	
    public long startTime = System.currentTimeMillis();
    public long last_used_seconds = 0;

    public long total_execs = 0;
    public long exec_us = 0;   
    
    public int total_skipped = 0; //including not triggered ones
    public int total_nontrigger = 0;
    public int total_bugs = 0;
    public int total_hangs = 0;
    
    public Set<Integer> testedUniqueCases = new HashSet<Integer>();
    public Set<String> fuzzedFiles = new HashSet<String>();
    
    public long total_bitmap_size = 0,         /* Total bit count for all bitmaps  */
    total_bitmap_entries = 0;      /* Number of bitmaps counted        */
    
    public HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public HashMap<Integer,Integer> timeToTotalCovs = new HashMap<Integer,Integer>();
    public long lastNewCovTime = 0;
    public int lastNewCovFaults = 0;
    
    public HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToNewCovTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsBugsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public long lastNewBugTime = 0;
    public int lastNewBugFaults = 0;
    
    public HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsHangsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public long lastNewHangTime = 0;
    public int lastNewHangFaults = 0;

    public FWHFuzzInfo() {
        reportWindow = FuzzInfo.reportWindow;
        startTime = FuzzInfo.startTime;
        last_used_seconds = FuzzInfo.last_used_seconds;
        total_execs = FuzzInfo.total_execs;
        exec_us = FuzzInfo.exec_us;
        total_skipped = FuzzInfo.total_skipped;
        total_nontrigger = FuzzInfo.total_nontrigger;
        total_bugs = FuzzInfo.total_bugs;
        total_hangs = FuzzInfo.total_hangs;
        testedUniqueCases = FuzzInfo.testedUniqueCases;
        fuzzedFiles = FuzzInfo.fuzzedFiles;
        total_bitmap_size = FuzzInfo.total_bitmap_size;
        total_bitmap_entries = FuzzInfo.total_bitmap_entries;
        timeToFaulsToTestsNum = FuzzInfo.timeToFaulsToTestsNum;
        timeToTotalCovs = FuzzInfo.timeToTotalCovs;
        lastNewCovTime = FuzzInfo.lastNewCovTime;
        lastNewCovFaults = FuzzInfo.lastNewCovFaults;
        timeToFaulsToNewCovTestsNum = FuzzInfo.timeToFaulsToNewCovTestsNum;
        timeToFaulsBugsNum = FuzzInfo.timeToFaulsBugsNum;
        lastNewBugTime = FuzzInfo.lastNewBugTime;
        lastNewBugFaults = FuzzInfo.lastNewBugFaults;
        timeToFaulsHangsNum = FuzzInfo.timeToFaulsHangsNum;
        lastNewHangTime = FuzzInfo.lastNewHangTime;
        lastNewHangFaults = FuzzInfo.lastNewHangFaults;
    }
}
