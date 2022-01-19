package edu.iscas.CCrashFuzzer;

import java.util.Set;

public class Fuzzer {
	public static int MAP_SIZE = 100;
	private final FuzzTarget target;
    private long executionsInSample;
    private long lastSampleTime;
    private long totalExecutions;
    private long totalSeedCases;
    private long totalCoverage;
    private long startTime;
    private Conf conf;
    private boolean allowRecovery;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    private int total_bugs;
    
    public static QueueEntry queue,     /* Fuzzing queue (linked list)      */
                              queue_cur, /* Current offset within the queue  */
                              queue_top, /* Top of the list                  */
                              q_prev100; /* Previous 100 marker              */
    
    public static QueueEntry[] top_rated = new QueueEntry[Fuzzer.MAP_SIZE]; /* Top entries for bitmap bytes     */
    
    /* Execution status fault codes */

    public static enum FaultCode{
      /* 00 */ FAULT_NONE,
      /* 01 */ FAULT_TMOUT,
      /* 02 */ FAULT_CRASH,
      /* 03 */ FAULT_ERROR,
      /* 04 */ FAULT_NOINST,
      /* 05 */ FAULT_NOBITS
    };
    
    public Fuzzer(FuzzTarget target, Conf conf, boolean recover) {
    	monitor = new Monitor(conf);
    	stat = new Stat();
    	this.target = target;
    	this.conf = conf;
    	allowRecovery = recover;
    	coverage = new CoverageCollector();
    	total_bugs = 0;
    	totalExecutions = 0;
    	totalSeedCases = 0;
    }

    public long getUsedMinutes() {
    	return (((System.currentTimeMillis()-startTime)/ 1000) / 60);
    }

	//from 0 to limit-1
	public int getRandomNumber(int limit) {
		int num = (int) (Math.random()*limit);
		return num;
	}
	
	/* Perform dry run of all test cases to confirm that the app is working as
	   expected. This is done only for the initial inputs, and only once. */

	public void perform_dry_run() {
		//for the first run
		target.run_target(FaultSequence.getEmptyIns(), conf, 1, 1);
		totalExecutions++;
		monitor.collectRunTimeRst("init", target.logInfo, FaultSequence.getEmptyIns());
//		MyXMLReader.read_trace_map(monitor.getRootReport("init")+"cov/cov.xml");
		coverage.read_bitmap(monitor.getRootReport("init")+"cov");
		int nb = coverage.has_new_bits();
		if(nb>0) {
			Stat.markNewCoverage(monitor.getRootReport("init"), nb);
			String fname = stat.saveInitStat();
			add_to_queue(monitor.getRootReport("init")+"fav-rst");
			totalSeedCases++;
		}
	}
	
	/* Take the current entry from the queue, fuzz it for a while. This
	   function is a tad too long... returns 0 if fuzzed successfully, 1 if
	   skipped or bailed out. */

	public boolean fuzz_one(QueueEntry q) {
		//generate mutations
//		Set<FaultSequence> mutates = Muatation.mutateFaultSequence(q);
		Set<FaultSequence> mutates = Muatation.mutateTwoSimilarSeq(q);
		//for each muatation{
		// common_fuzz_stuff
		//}
		
		boolean rst = false;
		for(FaultSequence seq:mutates) {
			rst = common_fuzz_stuff(seq);
		}
		return rst;
	}
	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public boolean common_fuzz_stuff(FaultSequence seq) {
		//save current test case to file
		//run_target
		//save_if_interesting
		String fname = stat.saveTestCase();
		int rst = -1;
		rst = target.run_target(seq, conf, totalExecutions+1, 1);
		totalExecutions++;
		save_if_interesting(seq, rst);
		return false;
	}
	
	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	public boolean save_if_interesting(FaultSequence seq, int faultMode) {
		//check current rst:
		//save bugs
		//add instereting test cases to queue
		monitor.collectRunTimeRst(String.valueOf(totalExecutions), target.logInfo, seq);
//		MyXMLReader.read_trace_map(monitor.getRootReport(String.valueOf(seq.hashCode()))+"cov/cov.xml");
		coverage.read_bitmap(monitor.getRootReport(String.valueOf(totalExecutions))+"cov");
		if(faultMode >0) {
			//save the bug report
			//cp it from root/case_ID/ to root/bugs/
			total_bugs++;
			Stat.markBug(monitor.getRootReport(String.valueOf(totalExecutions)));
		}
		int nb = coverage.has_new_bits();
		if(nb>0) {
			Stat.markNewCoverage(monitor.getRootReport(String.valueOf(totalExecutions)), nb);
//			add_to_queue(monitor.getRootReport(String.valueOf(totalExecutions))+"fav-rst");
//			queue_cur.has_new_cov = true;
//			queue_cur.was_fuzzed = false;
			totalSeedCases++;
		}
		return true;
	}
	
	/* Append new test case to the queue. */

	public void add_to_queue(String fname) {
		//read from file,add to queue
		TraceReader reader = new TraceReader(fname);
		reader.readTraces();
		QueueEntry entry = new QueueEntry();
		entry.ioSeq = reader.ioPoints;
		if(queue_cur == null) {
			queue_cur = entry;
		} else {
			entry.next = queue_cur;
			queue_cur = entry;
		}
	}
	
	public void cull_queue() {
		
	}
	
	public QueueEntry retrieveAnEntry() {
		if(queue_cur == null) {
			return null;
		} else {
			return queue_cur;
		}
	}
	
	public int calculate_score(QueueEntry q) {
		return 0;
		
	}
	
	public void start() {
		//fuzz loop:
		//perform dry run
		//while(1){
		//  cull_queue
		//  get an entry from queue
		//  fuzz_one
		//}
        this.totalCoverage = 0;
        this.totalExecutions = 0;
        this.executionsInSample = 0;
        this.lastSampleTime = System.currentTimeMillis();

        boolean hasFaultSequence = true;
        startTime = System.currentTimeMillis();
        
        perform_dry_run();
        
        while (( getUsedMinutes() < conf.maxTestMinutes) && hasFaultSequence) {
        	cull_queue();
        	QueueEntry q = retrieveAnEntry();
        	
        	boolean skipped_fuzz = fuzz_one(q);
        }
    }
}
