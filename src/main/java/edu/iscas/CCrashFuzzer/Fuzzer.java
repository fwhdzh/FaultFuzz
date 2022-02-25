package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class Fuzzer {
	public static int MAP_SIZE = 100;
	private final FuzzTarget target;
    private long executionsInSample;
    private long lastSampleTime;
    static long totalExecutions;
    static long total_exec_mins;
    static long a_exec_mins;
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
    public static List<QueueEntry> candidate_queue;
    public static List<QueueEntry> tested_queue;
    
    static long total_bitmap_size,         /* Total bit count for all bitmaps  */
    total_bitmap_entries;      /* Number of bitmaps counted        */
    
    long exec_us,                        /* Execution time (us)              */
    handicap,                       /* Number of queue cycles behind    */
    depth;                          /* Path depth                       */

   long queued_paths,              /* Total number of queued testcases */
           queued_variable,           /* Testcases with variable behavior */
           queued_at_start,           /* Total number of initial inputs   */
           queued_discovered,         /* Items discovered during this run */
           queued_imported,           /* Items imported via -S            */
           queued_favored,            /* Paths deemed favorable           */
           queued_with_cov,           /* Paths with new coverage bytes    */
           pending_not_fuzzed,        /* Queued but not done yet          */
           pending_favored,           /* Pending favored paths            */
           cur_skipped_paths,         /* Abandoned inputs in cur cycle    */
           cur_depth,                 /* Current path depth               */
           max_depth,                 /* Max path depth                   */
           useless_at_start,          /* Number of useless starting paths */
           var_byte_count,            /* Bitmap bytes with var behavior   */
           current_entry,             /* Current queue entry ID           */
           havoc_div = 1;             /* Cycle count divisor for havoc    */

   long total_crashes,             /* Total number of crashes          */
   unique_crashes,            /* Crashes with unique signatures   */
   total_tmouts,              /* Total number of timeouts         */
   unique_tmouts,             /* Timeouts with unique signatures  */
   unique_hangs,              /* Hangs with unique signatures     */
   total_execs,               /* Total execve() calls             */
   slowest_exec_ms,           /* Slowest testcase non hang in ms  */
   start_time,                /* Unix start time (ms)             */
   last_path_time,            /* Time for most recent path (ms)   */
   last_crash_time,           /* Time for most recent crash (ms)  */
   last_hang_time,            /* Time for most recent hang (ms)   */
   last_crash_execs,          /* Exec counter at last crash       */
   queue_cycle,               /* Queue round counter              */
   cycles_wo_finds,           /* Cycles without any new paths     */
   trim_execs,                /* Execs done to trim input files   */
   bytes_trim_in,             /* Bytes coming into the trimmer    */
   bytes_trim_out,            /* Bytes coming outa the trimmer    */
   blocks_eff_total,          /* Blocks subject to effector maps  */
   blocks_eff_select;         /* Blocks selected as fuzzable      */
   
   static long total_cal_us,              /* Total calibration time (us)      */
   total_cal_cycles;          /* Total calibration cycles         */
   
   boolean  skip_deterministic,        /* Skip deterministic stages?       */
   force_deterministic,       /* Force deterministic stages?      */
   use_splicing,              /* Recombine input files?           */
   dumb_mode,                 /* Run in non-instrumented mode?    */
   score_changed,             /* Scoring for favorites changed?   */
   kill_signal,               /* Signal that killed the child     */
   resuming_fuzz,             /* Resuming an older fuzzing job?   */
   timeout_given,             /* Specific timeout given?          */
   cpu_to_bind_given,         /* Specified cpu_to_bind given?     */
   not_on_tty,                /* stdout is not a tty              */
   term_too_small,            /* terminal dimensions too small    */
   uses_asan,                 /* Target uses ASAN?                */
   no_forkserver,             /* Disable forkserver?              */
   crash_mode,                /* Crash mode! Yeah!                */
   in_place_resume,           /* Attempt in-place resume?         */
   auto_changed,              /* Auto-generated tokens changed?   */
   no_cpu_meter_red,          /* Feng shui on the status screen   */
   no_arith,                  /* Skip most arithmetic ops         */
   shuffle_queue,             /* Shuffle input queue?             */
   bitmap_changed = true,        /* Time to update bitmap?           */
   qemu_mode,                 /* Running in QEMU mode?            */
   skip_requested,            /* Skip request, via SIGUSR1        */
   run_over10m,               /* Run time over 10 minutes?        */
   persistent_mode,           /* Running in persistent mode?      */
   deferred_mode,             /* Deferred forkserver mode?        */
   fast_cal;                  /* Try to calibrate faster?         */
   
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
    	candidate_queue = new ArrayList<QueueEntry>();
    	tested_queue = new ArrayList<QueueEntry>();
    	total_exec_mins = 0;
    	total_bitmap_size = 0;
        total_bitmap_entries = 0;
    }

    public long getUsedMinutes() {
    	return (((System.currentTimeMillis()-startTime)/ 1000) / 60);
    }
    
    public long getExecMinutes(long start) {
        return (((System.currentTimeMillis()-start)/ 1000) / 60);
    }

	//from 0 to limit-1
	public int getRandomNumber(int limit) {
		int num = (int) (Math.random()*limit);
		return num;
	}
	
	/* Perform dry run of all test cases to confirm that the app is working as
	   expected. This is done only for the initial inputs, and only once. */

	public void perform_first_run() {
		//for the first run
	    long start = System.currentTimeMillis();
		target.run_target(FaultSequence.getEmptyIns(), conf, "init");
		a_exec_mins = getExecMinutes(start);
		totalExecutions++;
		total_exec_mins += a_exec_mins;
//		monitor.generateAllFilesForTest("init", target.logInfo, FaultSequence.getEmptyIns());
		String tmpRootDir = monitor.getTmpReportDir("init");
		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo(tmpRootDir, target.logInfo, FaultSequence.getEmptyIns());
		
//		MyXMLReader.read_trace_map(monitor.getRootReport("init")+"cov/cov.xml");
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageName);
		int nb = coverage.has_new_bits();
		if(nb>0) {
//			Stat.markNewCoverage(monitor.getTmpReportDir("init"), nb);
			String fname = stat.saveInitStat();
			QueueEntry q = new QueueEntry();
			q.faultSeq = null;
			add_to_queue(q, monitor.getTmpReportDir("init")+"fav-rst");
			totalSeedCases++;
		}
		long usedMinutes = getUsedMinutes();
		FileUtil.copyToTested(conf.CUR_CRASH_FILE.getAbsolutePath(), String.valueOf(totalExecutions), usedMinutes);
		FileUtil.delete(conf.CUR_CRASH_FILE.getAbsolutePath());
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}
	}
	
	/* Take the current entry from the queue, fuzz it for a while. This
	   function is a tad too long... returns 0 if fuzzed successfully, 1 if
	   skipped or bailed out. */

	public boolean fuzz_one(QueueEntry q) {
		Stat.log("Going to fuzz q:"+q.faultSeq);
		//generate mutations
		List<QueueEntry> mutates = Mutation.mutateFaultSequence(q, conf);
//		List<QueueEntry> mutates = Muatation.mutateTwoSimilarSeq(q);
		List<QueueEntry> favored_mutates = mutates;
		List<QueueEntry> nonfavored_mutates = new ArrayList<>();
		
		boolean rst = false;
		
		while(!mutates.isEmpty()) {
			QueueEntry cur_mutate = null;
			
			Random r = new Random();
			int pick = r.nextInt(mutates.size());
			
//			int m_idx = 0;
//			for(; m_idx< mutates.size(); m_idx++) {
				QueueEntry tmp = mutates.get(pick);
				if(!tmp.favored && favored_mutates.size()> 0 &&
				         getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
					continue;
				}
				
				if(favored_mutates.isEmpty()) {
					int rand_num = getRandomNumber(100);
					if (rand_num < FuzzConf.SKIP_NFAV_NEW_PROB) {
						Stat.log("Jump back to queue: "+mutates.size()+" mutatations are not tested.");
						q.left_mutates_last_test = mutates.size();
						return true;
					}

		            if (rand_num < FuzzConf.SKIP_NFAV_OLD_PROB) {
		            	continue;
		            }
				}
				
				cur_mutate = tmp;
				mutates.remove(pick);
//				break;
//			}
			//retrive a mutation or skip to queue
			
			if(cur_mutate != null) {
				System.out.println("Going to test mutation "+pick);
				FaultPoint injected_fault = new FaultPoint();
				FaultPoint last_fault = cur_mutate.faultSeq.seq.get(cur_mutate.faultSeq.seq.size()-1);
				injected_fault.stat = last_fault.stat;
				injected_fault.ioPt = last_fault.ioPt;
				int exec_rst = common_fuzz_stuff(cur_mutate);
				
				if(exec_rst < 0) {
					mutates.add(cur_mutate);//not triggered in this time, try next time
					continue;
				}
				
				//adjust favored value
				int unfavored = 0;
				for(int i = 0; i< mutates.size(); i++) {
					FaultPoint adjacentPoint = mutates.get(i).faultSeq.seq.get(mutates.get(i).faultSeq.seq.size()-1);
					if(adjacentPoint.ioPt.CALLSTACK.toString().equals(injected_fault.ioPt.CALLSTACK.toString())){
						if((adjacentPoint.ioPt.TIMESTAMP<=injected_fault.ioPt.TIMESTAMP)
								&& (injected_fault.ioPt.TIMESTAMP-adjacentPoint.ioPt.TIMESTAMP)<conf.similarBehaviorWindow
								&& injected_fault.stat == adjacentPoint.stat) {
							mutates.get(i).favored = false;
							unfavored++;
						} else if (adjacentPoint.ioPt.TIMESTAMP>injected_fault.ioPt.TIMESTAMP) {
							if((adjacentPoint.ioPt.TIMESTAMP-injected_fault.ioPt.TIMESTAMP)<conf.similarBehaviorWindow
									&& injected_fault.stat == adjacentPoint.stat) {
								mutates.get(i).favored = false;
								unfavored++;
							} else {
								break;
							}
						}
					}
				}
				Stat.log(unfavored+" mutations are marked as unfavored.");
			}
		}
		q.was_fuzzed = true;
		Stat.log("Jump back to queue: "+mutates.size()+" mutatations are not tested.");
		q.left_mutates_last_test = mutates.size();
		return rst;
	}
	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueueEntry q) {
		//save current test case to file
		//run_target
		//save_if_interesting
		String fname = stat.saveTestCase();
		int rst = -1;
        long start = System.currentTimeMillis();
		rst = target.run_target(q.faultSeq, conf, String.valueOf(totalExecutions+1));
		a_exec_mins = getExecMinutes(start);
		totalExecutions++;
        total_exec_mins += a_exec_mins;
        save_if_interesting(q, rst);
		return rst;
	}
	
	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	/*
	 * Crashes and hangs are considered "unique" if the associated execution paths
	 * involve any state transitions not seen in previously-recorded faults. 
	 */
	public boolean save_if_interesting(QueueEntry q, int faultMode) {
		//check current rst:
		//save bugs
		//add instereting test cases to queue
//		monitor.generateAllFilesForTest(String.valueOf(totalExecutions), target.logInfo, q.faultSeq);
//		MyXMLReader.read_trace_map(monitor.getRootReport(String.valueOf(seq.hashCode()))+"cov/cov.xml");
		String tmpRootDir = monitor.getTmpReportDir(String.valueOf(totalExecutions));
//		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo(tmpRootDir, target.logInfo, q.faultSeq);
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageName);
		long usedMinutes = getUsedMinutes();
		if(faultMode >0) {
			//save the bug report
			//cp it from root/case_ID/ to root/bugs/
			total_bugs++;
			if(faultMode == 1) {
				FileUtil.copyDirToBugs(tmpRootDir, String.valueOf(totalExecutions), usedMinutes);
			} else if (faultMode == 2) {
				FileUtil.copyDirToHangs(tmpRootDir, String.valueOf(totalExecutions), usedMinutes);
			}
//			Stat.markBug(monitor.getTmpReportDir(String.valueOf(totalExecutions)));
		}
		int nb = coverage.has_new_bits();
		if(nb>0 || q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH) {//TODO: rethink this
//			Stat.markNewCoverage(tmpRootDir, nb);
			add_to_queue(q, tmpRootDir+FileUtil.ioTracesName);
//			queue_cur.has_new_cov = true;
//			queue_cur.was_fuzzed = false;
			totalSeedCases++;

			FileUtil.copyToQueue(tmpRootDir+conf.CUR_CRASH_FILE.getName(), String.valueOf(totalExecutions));
		}
		FileUtil.copyToTested(tmpRootDir+conf.CUR_CRASH_FILE.getName(), String.valueOf(totalExecutions), usedMinutes);
//		FileUtil.delete(conf.CUR_CRASH_FILE.getAbsolutePath());
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}
		return true;
	}
	
	/* Append new test case to the queue. */

	public void add_to_queue(QueueEntry q, String fname) {
		Stat.log("Add current entry to queue!");
		//after test, the retrieved ioSeq could be different from the original q.ioSeq
		//the actual faultSeq could also be different from the original q.faultSeq
		
		//read from file,add to queue
		TraceReader reader = new TraceReader(fname);
		reader.readTraces();
		q.ioSeq = reader.ioPoints;
		
		q.max_match_fault = 0;
		if(q.faultSeq == null || q.faultSeq.isEmpty()) {
			q.faultSeq = new FaultSequence();
			q.faultSeq.curFault = -1;
			q.faultSeq.seq = new ArrayList<FaultPoint>();
			q.candidate_io = 0;
		} else {
			//fix faultSeq
			//TODO: current comparison approch could cause problems
			//fault node in fault sequence should match the real node in io sequence
			for(q.candidate_io = 0; (q.candidate_io < q.ioSeq.size()) && q.max_match_fault<q.faultSeq.seq.size(); q.candidate_io++) {
				if(q.ioSeq.get(q.candidate_io).CALLSTACK.toString().equals(
						q.faultSeq.seq.get(q.max_match_fault).ioPt.CALLSTACK.toString())
						&& q.ioSeq.get(q.candidate_io).appearIdx == q.faultSeq.seq.get(q.max_match_fault).ioPt.appearIdx) {
					q.faultSeq.seq.get(q.max_match_fault).ioPt = q.ioSeq.get(q.candidate_io);
					q.faultSeq.seq.get(q.max_match_fault).tarNodeIp = q.faultSeq.seq.get(q.max_match_fault).actualNodeIp;
					q.faultSeq.seq.get(q.max_match_fault).actualNodeIp = null;
					q.max_match_fault++;
				}
			}
		}
		
		q.faultSeq.curAppear = 0;
		q.faultSeq.curFault = 0;
		q.depth = this.cur_depth + 1;
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		q.exec_m = a_exec_mins;
		
		total_bitmap_size += q.bitmap_size;
		total_bitmap_entries++;
		  
		if(q.depth > max_depth) {
		    max_depth = q.depth;
		}
		
		if (queue_top != null) {

		    queue_top.next = q;
		    queue_top = q;

		  } else {
		      q_prev100 = queue = queue_top = q;
		  }

		  queued_paths++;
		  pending_not_fuzzed++;

//		  cycles_wo_finds = 0;

		  /* Set next_100 pointer for every 100th element (index 0, 100, etc) to allow faster iteration. */
		  if ((queued_paths - 1) % 100 == 0 && queued_paths > 1) {

		    q_prev100.next_100 = q;
		    q_prev100 = q;

		  }

//		  last_path_time = get_cur_time();
		
		if(queue_cur == null) {
			queue_cur = q;
		} else {
			q.next = queue_cur;
			queue_cur = q;
		}
		candidate_queue.add(q);
	}
	
	/* When we bump into a new path, we call this to see if the path appears
	   more "favorable" than any of the existing ones. The purpose of the
	   "favorables" is to have a minimal set of paths that trigger all the bits
	   seen in the bitmap so far, and focus on fuzzing them at the expense of
	   the rest.
	   The first step of the process is to maintain a list of top_rated[] entries
	   for every byte in the bitmap. We win that slot if there is no previous
	   contender, or if the contender has a more favorable speed x size factor. */

	public void update_bitmap_score(QueueEntry q) {
	    
	}
	/* The second part of the mechanism discussed above is a routine that
	   goes over top_rated[] entries, and then sequentially grabs winners for
	   previously-unseen bytes (temp_v) and marks them as favored, at least
	   until the next run. The favored entries are given more air time during
	   all fuzzing steps. */
	public void cull_queue() {
	   
	}
	
	public QueueEntry retrieveAnEntry() {
	    Random rand = new Random();
	    int totalSum = 0;
	    for(QueueEntry q:candidate_queue) {
	        totalSum += q.getPerfScore();
	    }
	    
	    int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        while(sum < index ) {
             sum = sum + candidate_queue.get(i++).getPerfScore();
        }
        Stat.log("Retrieve entry:"+Math.max(0,i-1));
//        return candidate_queue.get(Math.max(0,i-1));
        return candidate_queue.remove(Math.max(0,i-1));
	    
//		if(queue_cur == null) {
//			return null;
//		} else {
//			return queue_cur;
//		}
	}
	
	public int calculate_score(QueueEntry q) {
		return 0;
		
	}
	
	public void start() {
	    int seek_to;
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
        
        perform_first_run();//now we only support one workload as input, in the future, we should 
       //support loading a series workloads as the initial input.
        
        int tmp = 5;
        while (( getUsedMinutes() < conf.maxTestMinutes) && hasFaultSequence && tmp>0) {
        	cull_queue();
        	
        	QueueEntry q = retrieveAnEntry();
        	
        	boolean skipped_fuzz = fuzz_one(q);
        	hasFaultSequence = !candidate_queue.isEmpty();
        }
    }
}
