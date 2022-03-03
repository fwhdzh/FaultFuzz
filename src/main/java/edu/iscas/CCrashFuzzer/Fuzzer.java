package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class Fuzzer {
	public static int MAP_SIZE = 100;
	private final FuzzTarget target;
    private long executionsInSample;
    private long lastSampleTime;
    static long last_used_seconds = 0;
    private long totalSeedCases;
    private long totalCoverage;
    private long startTime;
    private Conf conf;
    private boolean allowRecovery;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    private int total_skipped; //including not triggered ones
    private int total_nontrigger;
    private int total_bugs;
    private int total_hangs;
    public Set<Integer> testedUniqueCases;
    public Set<String> fuzzedFiles;
    
    public static QueueEntry queue,     /* Fuzzing queue (linked list)      */
                              queue_cur, /* Current offset within the queue  */
                              queue_top, /* Top of the list                  */
                              q_prev100; /* Previous 100 marker              */
    public static List<QueueEntry> candidate_queue;
    public static List<QueueEntry> fuzzed_queue;
    
    static long total_bitmap_size,         /* Total bit count for all bitmaps  */
    total_bitmap_entries;      /* Number of bitmaps counted        */
    
    static long exec_us;                          /* Path depth                       */
	long handicap;
	long depth;

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
   unique_hangs;         /* Blocks selected as fuzzable      */
static long total_execs;
long slowest_exec_ms;
long start_time;
long last_path_time;
long last_crash_time;
long last_hang_time;
long last_crash_execs;
long queue_cycle;
long cycles_wo_finds;
long trim_execs;
long bytes_trim_in;
long bytes_trim_out;
long blocks_eff_total;
long blocks_eff_select;
   
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
    	total_skipped = 0;
    	total_nontrigger = 0;
    	total_bugs = 0;
    	total_hangs = 0;
    	total_execs = 0;
    	totalSeedCases = 0;
    	candidate_queue = new ArrayList<QueueEntry>();
    	fuzzed_queue = new ArrayList<QueueEntry>();
    	exec_us = 0;
    	total_bitmap_size = 0;
        total_bitmap_entries = 0;
        last_used_seconds = 0;
        testedUniqueCases = new HashSet<>();
        fuzzedFiles = new HashSet<>();
    }

    public long getUsedSeconds() {
    	return last_used_seconds + (((System.currentTimeMillis()-startTime)/ 1000));
    }
    
    public static long getExecSeconds(long start) {
        return (((System.currentTimeMillis()-start)/ 1000));
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
		Stat.log("***********************Perform inital runs to collect IO traces*****************************");
	    long start = System.currentTimeMillis();
		target.run_target(FaultSequence.getEmptyIns(), conf, "init", conf.hangMinutes*60);
		total_execs++;
		exec_us += target.a_exec_seconds;
		String testID = "init";
		String tmpRootDir = monitor.getTmpReportDir(testID);
		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo("", testID, target.logInfo, FaultSequence.getEmptyIns());
		
//		MyXMLReader.read_trace_map(monitor.getRootReport("init")+"cov/cov.xml");
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		if(nb>0) {
//			Stat.markNewCoverage(monitor.getTmpReportDir("init"), nb);
			String fname = stat.saveInitStat();
			QueueEntry q = new QueueEntry();
			q.faultSeq = null;
			q.fname = testID;
			add_to_queue(q, testID);
			FileUtil.writePostTestInfo(q.fname, q.bitmap_size, q.exec_s);
			FileUtil.copyToQueue(q.fname, conf);
			totalSeedCases++;
		}
		long usedSeconds = getUsedSeconds();
		FileUtil.copyToTested(testID, usedSeconds, conf);
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}
        recordGlobalInfo();
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
			
			//retrive a mutation or skip to queue
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
					FileUtil.recordSkippedTests(q.fname, mutates, conf);
					total_skipped+=mutates.size();
					for(QueueEntry m:mutates) {
						if(m.was_tested) {
							total_nontrigger++;
						}
					}
					return true;
				}

	            if (rand_num < FuzzConf.SKIP_NFAV_OLD_PROB) {
	            	continue;
	            }
			}
			
			cur_mutate = tmp;
			mutates.remove(pick);
			
			if(cur_mutate != null) {
				System.out.println("Going to test mutation "+pick);
				FaultPoint injected_fault = new FaultPoint();
				FaultPoint last_fault = cur_mutate.faultSeq.seq.get(cur_mutate.faultSeq.seq.size()-1);
				injected_fault.stat = last_fault.stat;
				injected_fault.ioPt = last_fault.ioPt;
				int exec_rst = common_fuzz_stuff(cur_mutate, q.fname);
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
		total_skipped+=mutates.size();
		for(QueueEntry m:mutates) {
			if(m.was_tested) {
				total_nontrigger++;
			}
		}
		FileUtil.recordSkippedTests(q.fname, mutates, conf);
		return rst;
	}
	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueueEntry q, String seedName) {
		//save current test case to file
		//run_target
		//save_if_interesting
		String fname = stat.saveTestCase();
		int rst = -1;
        long start = System.currentTimeMillis();
        long waitTime = q.exec_s == 0L? conf.hangMinutes*60:q.exec_s*3;
		rst = target.run_target(q.faultSeq, conf, String.valueOf(total_execs+1), waitTime);
		q.fname = String.valueOf(total_execs+1);
		q.was_tested = true;
		total_execs++;
		exec_us += target.a_exec_seconds;
        save_if_interesting(q, rst, q.fname, seedName);
        
        if(rst == -1 || rst == 2) {//test again for not triggered cases and hang cases with a larger timeout
        	Stat.log("Try the test again, rst is "+rst+", not finished in "+waitTime+" seconds. New timeout is "+conf.hangMinutes*60);
        	if(Conf.MANUAL) {
            	Scanner scan = new Scanner(System.in);
            	scan.nextLine();
            }
        	q.faultSeq.reset();
        	int lastRst = rst;
        	String lastTestID = String.valueOf(total_execs);
        	start = System.currentTimeMillis();
    		rst = target.run_target(q.faultSeq, conf, lastTestID+"-retry", conf.hangMinutes*60);
    		q.fname = lastTestID+"-retry";
    		total_execs++;
    		exec_us += target.a_exec_seconds;
            save_if_interesting(q, rst, q.fname, seedName);
            
            if(lastRst == 2 && rst != 2) {//not a hang bug
            	FileUtil.removeFromHang(lastTestID,conf);
            	total_hangs--;
            }
        }
        
        testedUniqueCases.add(q.faultSeq.getFaultSeqID());
        
        recordGlobalInfo();

        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
        
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
	public boolean save_if_interesting(QueueEntry q, int faultMode, String testID, String seedName) {
		//check current rst:
		//save bugs
		//add instereting test cases to queue
//		monitor.generateAllFilesForTest(String.valueOf(totalExecutions), target.logInfo, q.faultSeq);
//		MyXMLReader.read_trace_map(monitor.getRootReport(String.valueOf(seq.hashCode()))+"cov/cov.xml");
//		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo(seedName,testID, target.logInfo, q.faultSeq);
		coverage.read_bitmap(FileUtil.root_tmp+testID+"/"+FileUtil.coverageDir);
		long usedSeconds = getUsedSeconds();
		if(faultMode >0) {
			//save the bug report
			//cp it from root/case_ID/ to root/bugs/
			if(faultMode == 1) {
				total_bugs++;
				Stat.log("*********************Find a BUG for test "+testID+"*********************");
				FileUtil.copyDirToBugs(testID, usedSeconds);
			} else if (faultMode == 2) {
				total_hangs++;
				Stat.log("*********************Find a HANG for test "+testID+"*********************");
				FileUtil.copyDirToHangs(testID, usedSeconds);
			}
//			Stat.markBug(monitor.getTmpReportDir(String.valueOf(totalExecutions)));
		} else if(faultMode <0) {
			Stat.log("*********************Test "+testID+" CANNOT be triggered*********************");
			FileUtil.copyToUntriggered(testID,conf);
			if(!Conf.DEBUG) {
				FileUtil.delete(FileUtil.root_tmp+testID);
			}
			return true;
		} else {
			int nb = coverage.has_new_bits();
			if(nb>0 || q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH) {//TODO: rethink this
//				Stat.markNewCoverage(tmpRootDir, nb);
				Stat.log("*********************Test "+testID+" is ADDED to queue*********************");
				add_to_queue(q, testID);
				FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);
//				queue_cur.has_new_cov = true;
//				queue_cur.was_fuzzed = false;
				totalSeedCases++;
				FileUtil.copyToQueue(testID, conf);
			}
		}
		FileUtil.copyToTested(testID, usedSeconds, conf);
//		FileUtil.delete(conf.CUR_CRASH_FILE.getAbsolutePath());
		if(!Conf.DEBUG) {
			FileUtil.delete(FileUtil.root_tmp+testID);
		}
		return true;
	}
	
	/* Append new test case to the queue. */

	public void add_to_queue(QueueEntry q, String fname) {
		//after test, the retrieved ioSeq could be different from the original q.ioSeq
		//the actual faultSeq could also be different from the original q.faultSeq
		
		//read from file,add to queue
		TraceReader reader = new TraceReader(FileUtil.root_tmp+fname+"/"+FileUtil.ioTracesDir);
		reader.readTraces();
		if(reader.ioPoints == null || reader.ioPoints.isEmpty()) {
			return;
		}
		q.ioSeq = reader.ioPoints;
		
		q.calibrate();
		
		q.depth = this.cur_depth + 1;
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		q.exec_s = target.a_exec_seconds;
		
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
        return candidate_queue.get(Math.max(0,i-1));
	    
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
        this.total_execs = 0;
        this.executionsInSample = 0;
        this.lastSampleTime = System.currentTimeMillis();

        RecoveryManager recover = new RecoveryManager();
        recover.loadQueue(candidate_queue, FileUtil.root_queue, conf);
        recover.loadFuzzed(fuzzedFiles, FileUtil.root_fuzzed, conf);
        
        boolean hasFaultSequence = true;
        startTime = System.currentTimeMillis();
        
        if(candidate_queue.isEmpty() && fuzzedFiles.isEmpty()) {
        	perform_first_run();//now we only support one workload as input, in the future, we should 
            //support loading a series workloads as the initial input.
        } else {
        	Stat.log("***********************Recover from last test!*****************************");
        	Stat.log("**-----------------------Queue size:"+candidate_queue.size()+"-----------------------**");
        	Stat.log("**-----------------------Fuzzed size:"+fuzzedFiles.size()+"-----------------------**");
        	loadGlobalInfo();
        	Stat.log("**-----------------------Cost testing time:"+FileUtil.parseSecondsToStringTime(this.last_used_seconds)+"-----------------------**");
        	Stat.log("**-----------------------Total target execution time:"+FileUtil.parseSecondsToStringTime(this.exec_us)+"-----------------------**");
        	Stat.log("**-----------------------Total target execution number:"+this.total_execs+"-----------------------**");
        	Stat.log("**-----------------------Total bitmap size:"+this.total_bitmap_size+"-----------------------**");
        	Stat.log("**-----------------------Total bitmap entries:"+this.total_bitmap_entries+"-----------------------**");
        	Stat.log("**-----------------------Virgin covered blocks:"+CoverageCollector.coveredBlocks(coverage.virgin_bits)+"-----------------------**");
        	Stat.log("****************************************************************************");
        }
        
        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
        
        int tmp = 5;
        while (( getUsedSeconds() < (conf.maxTestMinutes*60)) && hasFaultSequence && tmp>0) {
        	cull_queue();
        	
        	QueueEntry q = retrieveAnEntry();
        	
        	boolean skipped_fuzz = fuzz_one(q);
        	q.was_fuzzed = true;
        	candidate_queue.remove(q);
        	FileUtil.copyToFuzzed(q.fname, getUsedSeconds());
        	FileUtil.removeFromQueue(q.fname, conf);
        	fuzzedFiles.add(q.fname);
//        	fuzzed_queue.add(q);
        	
        	hasFaultSequence = !candidate_queue.isEmpty();
        }
        
        System.out.println(generateClientReport());
    }
	
	public void recordGlobalInfo() {
		//record total execution time, total used time, total execution number, total map size, total map entry
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root+FileUtil.exec_second_file);
			out.write(FileUtil.parseSecondsToStringTime(this.exec_us).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_execution_file);
			out.write(String.valueOf(this.total_execs).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_tested_time);
			out.write(FileUtil.parseSecondsToStringTime(getUsedSeconds()).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.traced_size_file);
			out.write(String.valueOf(this.total_bitmap_size).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_map_entry_file);
			out.write(String.valueOf(this.total_bitmap_entries).getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadGlobalInfo() {
		//load total execution time, total used time, total execution number, total map size, total map entry
		coverage.virgin_bits = coverage.load_a_bitmap(FileUtil.root+FileUtil.virgin_map_file);
		
		try {
			FileInputStream in = new FileInputStream(FileUtil.root+FileUtil.exec_second_file);
			byte[] content = new byte[1024];
			in.read(content);
			this.exec_us = FileUtil.parseStringTimeToSeconds((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_execution_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			this.total_execs = Long.parseLong((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_tested_time);
			Arrays.fill(content, (byte)0);
			in.read(content);
			this.last_used_seconds = FileUtil.parseStringTimeToSeconds((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.traced_size_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			this.total_bitmap_size = Long.parseLong((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_map_entry_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			this.total_bitmap_entries = Long.parseLong((new String(content)).trim());
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public String generateClientReport() {
		String rst = "";
		
		rst += "*******************************CrashFuzz Result**********************************\n";
		rst += "*Tested time: "+FileUtil.parseSecondsToStringTime(getUsedSeconds())+"\n";
		rst += "*For "+this.total_execs+" performed tests, the total execution time is "
		+FileUtil.parseSecondsToStringTime(this.exec_us)+", the average execution time is "
				+FileUtil.parseSecondsToStringTime(this.exec_us/this.total_execs)+"\n";
		rst += "*For "+this.total_bitmap_entries+" collected maps, the total map size is "
						+total_bitmap_size+", the average size of every map is "+(this.total_bitmap_size/this.total_bitmap_entries)
				+"\n";
		rst += "*Skip "+this.total_skipped+" tests, including "+this.total_nontrigger+" not triggered cases.";
		rst += "*Fuzzed "+this.fuzzedFiles.size()+" tests.";
		rst += "*Test "+this.testedUniqueCases.size()+" unique cases (different fault sequence IDs).";
		rst += "---------------------------------------------------------------------------------\n";
		rst += "*Got "+this.total_bugs+" bugs.";
		rst += "*Got "+this.total_hangs+" hangs.";
		rst += "*********************************************************************************\n";
		return rst;
	}
}
