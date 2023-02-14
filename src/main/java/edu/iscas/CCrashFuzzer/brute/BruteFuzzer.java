package edu.iscas.CCrashFuzzer.brute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.CoverageCollector;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.FuzzConf;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.FuzzInfo;
import edu.iscas.CCrashFuzzer.FuzzTarget;
import edu.iscas.CCrashFuzzer.Fuzzer;
import edu.iscas.CCrashFuzzer.IOPoint;
import edu.iscas.CCrashFuzzer.Monitor;
import edu.iscas.CCrashFuzzer.Mutation;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.QueueManagerNew;
import edu.iscas.CCrashFuzzer.QueueManagerNew.QueuePair;
import edu.iscas.CCrashFuzzer.RecoveryManager;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.TraceReader;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class BruteFuzzer {
	public static int MAP_SIZE = 100;
	private final FuzzTarget target;
    private long totalSeedCases;
    private Conf conf;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    public static boolean running = false;
    
    public static QueueEntry queue,     /* Fuzzing queue (linked list)      */
                              queue_cur, /* Current offset within the queue  */
                              queue_top, /* Top of the list                  */
                              q_prev100; /* Previous 100 marker              */
    public static List<QueueEntry> candidate_queue;
    public static List<QueueEntry> fuzzed_queue;
    
    
                           /* Path depth                       */
	long handicap;
	long depth;

   long queued_paths,              /* Total number of queued testcases */
           cur_depth,                 /* Current path depth               */
           max_depth;                 /* Max path depth                   */
   
   int queue_cycle;
   
    public static QueueEntry[] top_rated = new QueueEntry[BruteFuzzer.MAP_SIZE]; /* Top entries for bitmap bytes     */
    
    /* Execution status fault codes */

    public static enum FaultCode{
      /* 00 */ FAULT_NONE,
      /* 01 */ FAULT_TMOUT,
      /* 02 */ FAULT_CRASH,
      /* 03 */ FAULT_ERROR,
      /* 04 */ FAULT_NOINST,
      /* 05 */ FAULT_NOBITS
    };
    
    public BruteFuzzer(FuzzTarget target, Conf conf, boolean recover) {
    	monitor = new Monitor(conf);
    	stat = new Stat();
    	this.target = target;
    	this.conf = conf;
    	coverage = new CoverageCollector();
    	totalSeedCases = 0;
    	candidate_queue = new ArrayList<QueueEntry>();
    	fuzzed_queue = new ArrayList<QueueEntry>();
    	queue_cycle = 0;
    }

    public static long getExecSeconds(long start) {
        return (((System.currentTimeMillis()-start)/ 1000));
    }

	//from 0 to limit-1
	public static int getRandomNumber(int limit) {
		int num = (int) (Math.random()*limit);
		return num;
	}
	
	/* Perform dry run of all test cases to confirm that the app is working as
	   expected. This is done only for the initial inputs, and only once. */

	public void perform_first_run() {
		//for the first run
		Stat.log("***********************Perform inital runs to collect IO traces*****************************");
	    long start = System.currentTimeMillis();
		String testID = "init";
		target.run_target(FaultSequence.getEmptyIns(), conf, "init", conf.hangSeconds);
		FuzzInfo.total_execs++;
		FuzzInfo.exec_us += target.a_exec_seconds;
		HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToTests.computeIfAbsent(0, key -> 0);
		faultsToTests.computeIfPresent(0, (key, value) -> value + 1);
		String tmpRootDir = monitor.getTmpReportDir(testID);
		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo("", testID, target.logInfo, FaultSequence.getEmptyIns());
		
//		MyXMLReader.read_trace_map(monitor.getRootReport("init")+"cov/cov.xml");
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
		QueueEntry q = new QueueEntry();
		q.faultSeq = null;
		q.fname = testID;
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		q.exec_s = target.a_exec_seconds;
		if(nb>0) {
			FuzzInfo.lastNewCovFaults = 0;
			HashMap<Integer, Integer> faultsToNewCovTests = FuzzInfo.timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
			faultsToNewCovTests.computeIfAbsent(0, key -> 0);
			faultsToNewCovTests.computeIfPresent(0, (key, value) -> value + 1);
			
			add_to_queue(q, testID);
			if(q.recovery_io_id == null || q.recovery_io_id.isEmpty()) {
				q.recovery_io_id = new HashSet<Integer>();
			}
			Mutation.mutateFaultSequence(q, conf);
			candidate_queue.addAll(q.mutates);
			
			q.has_new_cov = true;
			q.handicap = 0;
			q.fuzzed_time = 0;
			q.was_fuzzed = false;
			q.new_cov_contribution = nb;
			
			FileUtil.writePostTestInfo(q.fname, q.bitmap_size, q.exec_s);
			FileUtil.copyToQueue(q.fname, conf);
			totalSeedCases++;
		}
		FuzzInfo.total_bitmap_size += q.bitmap_size;
		FuzzInfo.total_bitmap_entries++;
		long usedSeconds = FuzzInfo.getUsedSeconds();
		FileUtil.copyToTested(testID, usedSeconds, conf);
		if(Conf.MANUAL) {
			Scanner scan = new Scanner(System.in);
        	scan.nextLine();
		}
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}
        recordGlobalInfo();
	}
	
	/* Take the current entry from the queue, fuzz it for a while. This
	   function is a tad too long... returns 0 if fuzzed successfully, 1 if
	   skipped or bailed out. */

	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueueEntry q, QueueEntry seedQ) {
		//save current test case to file
		//run_target
		//save_if_interesting
		String fname = stat.saveTestCase();
		int rst = -1;
        long start = System.currentTimeMillis();
        
//        long waitTime = q.exec_s == 0L? conf.hangSeconds*2:q.exec_s*3;
        long waitTime = conf.hangSeconds > q.exec_s*3? conf.hangSeconds : q.exec_s*3;
        String testID = String.valueOf(FuzzInfo.total_execs+1)+"_"+q.faultSeq.seq.size()+"f";
		rst = target.run_target(q.faultSeq, conf, testID, waitTime);
		q.fname = testID;
		q.was_tested = true;
		FuzzInfo.total_execs++;
		FuzzInfo.exec_us += target.a_exec_seconds;
		if(rst != -1) {
			HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
			faultsToTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
			faultsToTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
		}
        save_if_interesting(q, rst, q.fname, seedQ);
        
        if(rst == 2) {//test again for hang cases with a larger timeout
        	Stat.log("Try the test again, rst is "+rst+", not finished in "+waitTime+" seconds. New timeout is "+conf.hangSeconds*60);
        	if(Conf.MANUAL) {
            	Scanner scan = new Scanner(System.in);
            	scan.nextLine();
            }
        	q.faultSeq.reset();
        	int lastRst = rst;
        	start = System.currentTimeMillis();
    		rst = target.run_target(q.faultSeq, conf, testID+"-retry", conf.hangSeconds*2);
    		q.fname = testID+"-retry";
    		FuzzInfo.total_execs++;
    		FuzzInfo.exec_us += target.a_exec_seconds;
    		if(rst != -1) {
    			HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
    			faultsToTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    			faultsToTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    		}
            save_if_interesting(q, rst, q.fname, seedQ);
            
            if(lastRst == 2 && rst != 2) {//not a hang bug
            	FileUtil.removeFromHang(testID,conf);
            	FuzzInfo.total_hangs--;
            } else if (lastRst == 2 && rst == 2) {
				FuzzInfo.lastNewHangTime = FuzzInfo.getUsedSeconds();
				FuzzInfo.lastNewHangFaults = q.faultSeq.seq.size();
				HashMap<Integer, Integer> faultsToHangs = FuzzInfo.timeToFaulsHangsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
				faultsToHangs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
				faultsToHangs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
            }
        }
        
        FuzzInfo.testedUniqueCases.add(q.faultSeq.getFaultSeqID());
        
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
	public boolean save_if_interesting(QueueEntry q, int faultMode, String testID, QueueEntry seedQ) {
		//check current rst:
		//save bugs
		//add instereting test cases to queue
//		monitor.generateAllFilesForTest(String.valueOf(totalExecutions), target.logInfo, q.faultSeq);
//		MyXMLReader.read_trace_map(monitor.getRootReport(String.valueOf(seq.hashCode()))+"cov/cov.xml");
//		FileUtil.copyFileToDir(conf.CUR_CRASH_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo(seedQ.fname, testID, target.logInfo, q.faultSeq);
		coverage.read_bitmap(FileUtil.root_tmp+testID+"/"+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		q.new_cov_contribution = nb;
		if(nb>0 && faultMode != -1) {
			FuzzInfo.lastNewCovFaults = q.faultSeq.seq.size();
			HashMap<Integer, Integer> faultsToNewCovTests = FuzzInfo.timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
			faultsToNewCovTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
			faultsToNewCovTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);

			q.has_new_cov = true;
		}
		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
		FileUtil.writeNeighborNewCovs(testID, q.faultSeq.adjacent_new_covs);
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		q.exec_s = target.a_exec_seconds;
		FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);
		long usedSeconds = FuzzInfo.getUsedSeconds();
		if(faultMode >0) {
			//save the bug report
			//cp it from root/case_ID/ to root/bugs/
			if(faultMode == 1) {
				FuzzInfo.total_bugs++;
				Stat.log("*********************Find a BUG for test "+testID+"*********************");
				FuzzInfo.lastNewBugTime = FuzzInfo.getUsedSeconds();
				FuzzInfo.lastNewBugFaults = q.faultSeq.seq.size();
				HashMap<Integer, Integer> faultsToBugs = FuzzInfo.timeToFaulsBugsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
				faultsToBugs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
				faultsToBugs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
				FileUtil.copyDirToBugs(testID, usedSeconds);
			} else if (faultMode == 2) {
				FuzzInfo.total_hangs++;
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
			if(nb>0 || q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH) {//TODO: rethink this
//				Stat.markNewCoverage(tmpRootDir, nb);
			}
			Stat.log("*********************Test "+testID+" is ADDED to queue*********************");
			add_to_queue(q, testID);
			if(q.recovery_io_id == null || q.recovery_io_id.isEmpty()) {
				q.recovery_io_id = new HashSet<Integer>();
				q.recovery_io_id.addAll(q.unique_io_id);
				q.recovery_io_id.removeAll(seedQ.unique_io_id);
			}
			Mutation.mutateFaultSequence(q, conf);
			candidate_queue.addAll(q.mutates);
			
//			queue_cur.has_new_cov = true;
//			queue_cur.was_fuzzed = false;
			totalSeedCases++;
			FileUtil.copyToQueue(testID, conf);
		}
		FuzzInfo.total_bitmap_size += q.bitmap_size;
		FuzzInfo.total_bitmap_entries++;
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
		
		if(q.unique_io_id == null || q.unique_io_id.isEmpty()) {
			q.unique_io_id = new HashSet<Integer>();
			for(IOPoint p:q.ioSeq) {
				q.unique_io_id.add(p.ioID);
			}
		}
		
		q.calibrate();
		
		q.depth = this.cur_depth + 1;
		q.handicap = 0;
		q.was_fuzzed = false;
		q.fuzzed_time = 0;
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
	   for(QueueEntry q:candidate_queue) {
		   for(QueueEntry m:q.mutates) {
			   m.handicap++;
		   }
	   }
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
        FuzzInfo.total_execs = 0;
        
        RecoveryManager recover = new RecoveryManager();
        recover.loadQueue(candidate_queue, FileUtil.root_queue, conf);
        recover.loadFuzzed(FuzzInfo.fuzzedFiles, FileUtil.root_fuzzed, conf);
        
        boolean hasFaultSequence = true;
        FuzzInfo.startTime = System.currentTimeMillis();

        Thread observer = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					while (( FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes*60))) {
						FileOutputStream out = new FileOutputStream(FileUtil.root+FileUtil.report_file);
						String report = FuzzInfo.generateClientReport();
						out.write(report.getBytes());
						out.flush();
						out.close();
						
						Thread.currentThread().sleep(1000);
					}
					System.out.println(FuzzInfo.generateClientReport());
					System.exit(0);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        };
        observer.start();
        
        perform_first_run();
//        if(candidate_queue.isEmpty() && FuzzInfo.fuzzedFiles.isEmpty()) {
//        	perform_first_run();//now we only support one workload as input, in the future, we should 
//            //support loading a series workloads as the initial input.
//        } else {
//        	Stat.log("***********************Recover from last test!*****************************");
//        	Stat.log("**-----------------------Queue size:"+candidate_queue.size()+"-----------------------**");
//        	Stat.log("**-----------------------Fuzzed size:"+FuzzInfo.fuzzedFiles.size()+"-----------------------**");
//        	loadGlobalInfo();
//        	Stat.log("**-----------------------Cost testing time:"+FileUtil.parseSecondsToStringTime(FuzzInfo.last_used_seconds)+"-----------------------**");
//        	Stat.log("**-----------------------Total target execution time:"+FileUtil.parseSecondsToStringTime(FuzzInfo.exec_us)+"-----------------------**");
//        	Stat.log("**-----------------------Total target execution number:"+FuzzInfo.total_execs+"-----------------------**");
//        	Stat.log("**-----------------------Total bitmap size:"+FuzzInfo.total_bitmap_size+"-----------------------**");
//        	Stat.log("**-----------------------Total bitmap entries:"+FuzzInfo.total_bitmap_entries+"-----------------------**");
//        	Stat.log("**-----------------------Virgin covered blocks:"+CoverageCollector.coveredBlocks(coverage.virgin_bits)+"-----------------------**");
//        	Stat.log("****************************************************************************");
//        }
        
        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
        
        while (( FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes*60)) && !candidate_queue.isEmpty()) {
        	queue_cycle++;
//        	cull_queue();
        	
        	Random rand = new Random();
        	int index = rand.nextInt(candidate_queue.size());
        	
        	QueueEntry q = candidate_queue.get(index);
        	if(q == null) {
        		break;
        	}
        	Stat.log("Going to test first queue entry");
        	
        	int exec_rst = common_fuzz_stuff(q, q);
        	
        	if(exec_rst != -1) {
        		candidate_queue.remove(index);
            	FileUtil.removeFromQueue(q.fname, conf);
            	FuzzInfo.fuzzedFiles.add(q.fname);
            	FileUtil.copyToFuzzed(q.fname, FuzzInfo.getUsedSeconds());
        	}
        	
        	
        	if(Conf.MANUAL) {
    			Scanner scan = new Scanner(System.in);
            	scan.nextLine();
    		}
//        	fuzzed_queue.add(q);
        	
        	hasFaultSequence = !candidate_queue.isEmpty();
        }
        
        System.out.println(FuzzInfo.generateClientReport());
    }
	
	public void recordGlobalInfo() {
		//record total execution time, total used time, total execution number, total map size, total map entry
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root+FileUtil.exec_second_file);
			out.write(FileUtil.parseSecondsToStringTime(FuzzInfo.exec_us).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_execution_file);
			out.write(String.valueOf(FuzzInfo.total_execs).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_tested_time);
			out.write(FileUtil.parseSecondsToStringTime(FuzzInfo.getUsedSeconds()).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.traced_size_file);
			out.write(String.valueOf(FuzzInfo.total_bitmap_size).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root+FileUtil.total_map_entry_file);
			out.write(String.valueOf(FuzzInfo.total_bitmap_entries).getBytes());
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
			FuzzInfo.exec_us = FileUtil.parseStringTimeToSeconds((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_execution_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			FuzzInfo.total_execs = Long.parseLong((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_tested_time);
			Arrays.fill(content, (byte)0);
			in.read(content);
			FuzzInfo.last_used_seconds = FileUtil.parseStringTimeToSeconds((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.traced_size_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			FuzzInfo.total_bitmap_size = Long.parseLong((new String(content)).trim());
			in.close();
			
			in = new FileInputStream(FileUtil.root+FileUtil.total_map_entry_file);
			Arrays.fill(content, (byte)0);
			in.read(content);
			FuzzInfo.total_bitmap_entries = Long.parseLong((new String(content)).trim());
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
