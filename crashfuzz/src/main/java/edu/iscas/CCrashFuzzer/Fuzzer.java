package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.QueueManagerNew.QueuePair;
import edu.iscas.CCrashFuzzer.control.NormalTarget;
import edu.iscas.CCrashFuzzer.control.AbstractDeterminismTarget.FaultSeqAndIOSeq;
import edu.iscas.CCrashFuzzer.control.determine.TryBestDeterminismTarget;
import edu.iscas.CCrashFuzzer.control.determine.TryBestDeterminismTarget.TryBestDeterminismTResult;
import edu.iscas.CCrashFuzzer.control.replay.ReplayTarget;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class Fuzzer {
	public static int MAP_SIZE = 100;
	private final NormalTarget target;
    private long totalSeedCases;
    Conf conf;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    public static boolean running = false;
    
    // public static QueueEntry queue,     /* Fuzzing queue (linked list)      */
    //                           queue_cur, /* Current offset within the queue  */
    //                           queue_top, /* Top of the list                  */
    //                           q_prev100; /* Previous 100 marker              */
    public static List<QueueEntry> candidate_queue;
    public static List<QueueEntry> fuzzed_queue;
    
    
                           /* Path depth                       */
	long handicap;
	long depth;

//    long queued_paths,              /* Total number of queued testcases */
//            cur_depth,                 /* Current path depth               */
//            max_depth;                 /* Max path depth                   */
   
   int queue_cycle;
   
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
    
	RecoveryManagerFWH recoveryManager;

    public Fuzzer(NormalTarget target, Conf conf, boolean recover) {
    	monitor = new Monitor(conf);
    	stat = new Stat();
    	this.target = target;
    	this.conf = conf;
    	coverage = new CoverageCollector();
    	totalSeedCases = 0;
    	candidate_queue = new ArrayList<QueueEntry>();
    	fuzzed_queue = new ArrayList<QueueEntry>();
    	queue_cycle = 0;

		recoveryManager = new RecoveryManagerFWH();
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
		FaultSequence empty = FaultSequence.getEmptyIns();
		QueueEntry q = new QueueEntry();
		q.faultSeq = empty;
		q.fname = testID;
		q.ioSeq = new ArrayList<>();

		NormalTarget target = new NormalTarget();
		int rst = -1;
		rst = target.run_target(empty, conf, "init", conf.hangSeconds);

		// TryBestDeterminismTarget target = new TryBestDeterminismTarget();
		// FaultSeqAndIOSeq faultSeqAndIOSeq = new FaultSeqAndIOSeq(q.faultSeq, q.ioSeq);
		// target.beforeTarget(faultSeqAndIOSeq, conf, "init", conf.DETERMINE_WAIT_TIME);
		// target.doTarget();
		// TryBestDeterminismTResult tbdResult = target.afterTarget();
		// int rst = tbdResult.result;

		String tmpRootDir = monitor.getTmpReportDir(testID);
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();

		updateQInSaveIfInterestring(q, rst, testID, nb, target.a_exec_seconds);
		
		if (nb > 0) {
			add_to_queue(q, testID);
			if(q.unique_io_id == null || q.unique_io_id.isEmpty()) {
				q.unique_io_id = new HashSet<Integer>();
				for(IOPoint p:q.ioSeq) {
					q.unique_io_id.add(p.ioID);
				}
			}
			if(q.recovery_io_id == null || q.recovery_io_id.isEmpty()) {
				q.recovery_io_id = new HashSet<Integer>();
			}
			Mutation.initializeFaultPointsToMutate(q, conf);
			Mutation.initializeLocalNotTestedFaultId(q);
			// Mutation.mutateFaultSequence(q, conf);
			totalSeedCases++;
		}

		updateFuzzInfoInSaveIfInterestring(q, rst, testID, target.a_exec_seconds, nb);

		writeToFileInSaveIfInterestring(q, rst, testID, "", nb, target.logInfo);
		
		if(Conf.MANUAL) {
			Scanner scan = new Scanner(System.in);
        	scan.nextLine();
		}
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}

        recordGlobalInfo();

		copyLogsToControllerWithTestId(testID);
	}

	
	/* Take the current entry from the queue, fuzz it for a while. This
	   function is a tad too long... returns 0 if fuzzed successfully, 1 if
	   skipped or bailed out. */

	public void update_queue(QueuePair q) {

		q.seed.faultPointsToMutate.remove(q.mutateIdx);

    	q.seed.mutates.remove(q.mutateIdx);
    	if(q.mutate.favored) {
    		q.seed.favored_mutates.remove(q.mutate);
    	}
    	if(q.mutate.faultSeq.on_recovery) {
    		q.seed.on_recovery_mutates.remove(q.mutate);
    	}
    	
    	FaultPoint tmpLastFault = q.mutate.faultSeq.seq.get(q.mutate.faultSeq.seq.size()-1);
		int tmpID = tmpLastFault.getFaultID();
		q.seed.not_tested_fault_id.remove(tmpID);
		QueueManagerNew.tested_fault_id.add(tmpID);
    	
		FaultPoint injected_fault = tmpLastFault;
		
		int startLoc = (q.mutateIdx-10)>=0? q.mutateIdx-10:0;
		long tmpTime = q.seed.mutates.get(startLoc).faultSeq.seq.get(q.seed.mutates.get(startLoc).faultSeq.seq.size()-1).ioPt.TIMESTAMP;
		while((injected_fault.ioPt.TIMESTAMP - tmpTime) < conf.similarBehaviorWindow
				&& startLoc != 0) {
			startLoc = (startLoc-10)>=0? startLoc-10:0;
			tmpTime = q.seed.mutates.get(startLoc).faultSeq.seq.get(q.seed.mutates.get(startLoc).faultSeq.seq.size()-1).ioPt.TIMESTAMP;
		}
		
		int unfavored = 0;
		for(int i = startLoc; i< q.seed.mutates.size(); i++) {
			FaultPoint adjacentPoint = q.seed.mutates.get(i).faultSeq.seq.get(q.seed.mutates.get(i).faultSeq.seq.size()-1);
			if((adjacentPoint.ioPt.TIMESTAMP<=injected_fault.ioPt.TIMESTAMP)
					&& (injected_fault.ioPt.TIMESTAMP-adjacentPoint.ioPt.TIMESTAMP)<conf.similarBehaviorWindow
					&& injected_fault.stat == adjacentPoint.stat
					&& q.seed.mutates.get(i).favored) {
				if(adjacentPoint.ioPt.CALLSTACK.toString().equals(injected_fault.ioPt.CALLSTACK.toString())
						|| (!injected_fault.ioPt.PATH.startsWith("FAVMSG")
								&& !adjacentPoint.ioPt.PATH.startsWith("FAVMSG")
								&& injected_fault.ioPt.PATH.equals(adjacentPoint.ioPt.PATH))){
					q.seed.favored_mutates.remove(q.seed.mutates.get(i));
					q.seed.mutates.get(i).favored = false;
					unfavored++;
				}
			} else if (adjacentPoint.ioPt.TIMESTAMP>injected_fault.ioPt.TIMESTAMP) {
				if((adjacentPoint.ioPt.TIMESTAMP-injected_fault.ioPt.TIMESTAMP)<conf.similarBehaviorWindow
						&& injected_fault.stat == adjacentPoint.stat
						&& q.seed.mutates.get(i).favored) {
					if(adjacentPoint.ioPt.CALLSTACK.toString().equals(injected_fault.ioPt.CALLSTACK.toString())
							|| (!injected_fault.ioPt.PATH.startsWith("FAVMSG")
									&& !adjacentPoint.ioPt.PATH.startsWith("FAVMSG")
									&& injected_fault.ioPt.PATH.equals(adjacentPoint.ioPt.PATH))){
						q.seed.favored_mutates.remove(q.seed.mutates.get(i));
						q.seed.mutates.get(i).favored = false;
						unfavored++;
					}
				} else {
					break;
				}
			}
		}
		Stat.log(unfavored+" mutations are marked as unfavored.");
		
		if(q.seed.mutates == null || q.seed.mutates.isEmpty()) {
    		candidate_queue.remove(q.seedIdx);
        	FileUtil.removeFromQueue(q.seed.fname, conf);
        	FuzzInfo.fuzzedFiles.add(q.seed.fname);
        	FileUtil.copyToFuzzed(q.seed.fname, FuzzInfo.getUsedSeconds());
    	}
	}

	private int callTargetAndGetRst() {
		int result = 0;
		return result;
	}
	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueueEntry q, QueueEntry seedQ) {
		//save current test case to file
		//run_target
		//save_if_interesting
		// String fname = stat.saveTestCase();
		
        long start = System.currentTimeMillis();
        
//        long waitTime = q.exec_s == 0L? conf.hangSeconds*2:q.exec_s*3;
        long waitTime = conf.hangSeconds > q.exec_s*3? conf.hangSeconds : q.exec_s*3;	
        String testID = String.valueOf(FuzzInfo.total_execs+1)+"_"+q.faultSeq.seq.size()+"f";
		int rst = -1;

		TryBestDeterminismTarget target = new TryBestDeterminismTarget();
		FaultSeqAndIOSeq faultSeqAndIOSeq = new FaultSeqAndIOSeq(q.faultSeq, q.ioSeq);
		target.beforeTarget(faultSeqAndIOSeq, conf, testID, waitTime);
		target.doTarget();
		TryBestDeterminismTResult tbdResult = target.afterTarget();
		// rst = tbdResult.result;
		rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.result);

		// NormalTarget target = new NormalTarget();
		// rst = target.run_target(q.faultSeq, conf, testID, waitTime);

		q.fname = testID;
		q.was_tested = true;
		
		
		
        // save_if_interesting(q, rst, q.fname, seedQ);
		save_if_interesting_rewrite(q, rst, testID, seedQ, target.a_exec_seconds, target.logInfo);
        
        if(rst == 2) {//test again for hang cases with a larger timeout
        	Stat.log("Try the test again, rst is "+rst+", not finished in "+waitTime+" seconds. New timeout is "+conf.hangSeconds*60);
        	if(Conf.MANUAL) {
            	Scanner scan = new Scanner(System.in);
            	scan.nextLine();
            }
			int lastRst = rst;
			start = System.currentTimeMillis();
        	q.faultSeq.reset();

    		// rst = target.run_target(q.faultSeq, conf, testID+"-retry", conf.hangSeconds*2);

			target.beforeTarget(faultSeqAndIOSeq, conf, testID+"-retry", conf.hangSeconds*2);
			target.doTarget();
			tbdResult = target.afterTarget();
			// rst = tbdResult.result;
			rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.result);


			q.fname = testID+"-retry";

			if(lastRst == 2 && rst != 2){
				FuzzInfo.total_hangs--;
				FileUtil.removeFromHang(testID,conf);
			}
			if (lastRst == 2 && rst == 2) {
				FuzzInfo.lastNewHangTime = FuzzInfo.getUsedSeconds();
				FuzzInfo.lastNewHangFaults = q.faultSeq.seq.size();
				updateTimeToFaulsHangsNum(q);
            }

            // save_if_interesting(q, rst, q.fname, seedQ);
			// save_if_interesting_rewrite(q, rst, q.fname, seedQ, target);
			save_if_interesting_rewrite(q, rst, testID, seedQ, target.a_exec_seconds, target.logInfo);
            

			
        }
        
        FuzzInfo.testedUniqueCases.add(q.faultSeq.getFaultSeqID());
        
        recordGlobalInfo();

        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }

		copyLogsToControllerWithTestId(testID);
        
		return rst;
	}
	
	private void updateTimeToFaulsHangsNum(QueueEntry q) {
		HashMap<Integer, Integer> faultsToHangs = FuzzInfo.timeToFaulsHangsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToHangs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
		faultsToHangs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
	}

	private void updateTimeToFaulsToTestsNum(QueueEntry q) {
		updateTimeToFaulsToTestsNum(q.faultSeq);
	}

	private void updateTimeToFaulsToTestsNum(FaultSequence fs) {
		HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToTests.computeIfAbsent(fs.seq.size(), key -> 0);
		faultsToTests.computeIfPresent(fs.seq.size(), (key, value) -> value + 1);
	}

	private void updateTimeToFaulsToNewCovTestsNum(QueueEntry q) {
		HashMap<Integer, Integer> faultsToNewCovTests = FuzzInfo.timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToNewCovTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
		faultsToNewCovTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
	}

	private void updateTimeToFaulsBugsNum(QueueEntry q) {
		HashMap<Integer, Integer> faultsToBugs = FuzzInfo.timeToFaulsBugsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToBugs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
		faultsToBugs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
	}

	private void updateQInSaveIfInterestring(QueueEntry q, int faultMode, String testID, QueueEntry seedQ, int nb, long exex_s) {
		updateQInSaveIfInterestring(seedQ, faultMode, testID, nb, exex_s);
	}

	private void updateQInSaveIfInterestring(QueueEntry q, int faultMode, String testID, int nb, long exec_s) {
		// int nb = coverage.has_new_bits();
		q.new_cov_contribution = nb;
		if(nb>0 && faultMode != -1) {
			q.has_new_cov = true;
		}
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		// q.exec_s = target.a_exec_seconds;
		q.exec_s = exec_s;
	}

	private void addToQueueAndMutateInSaveIfInterestring(QueueEntry q, String testID, QueueEntry seedQ) {
		add_to_queue(q, testID);
		initializeRecoveryIOIdWithUniqueIOId(q, seedQ);
		Mutation.initializeFaultPointsToMutate(q, conf);
		Mutation.initializeLocalNotTestedFaultId(q);
		// Mutation.mutateFaultSequence(q, conf);
		totalSeedCases++;
	}

	public void initializeRecoveryIOIdWithUniqueIOId(QueueEntry q, QueueEntry seedQ) {
		if(q.unique_io_id == null || q.unique_io_id.isEmpty()) {
			q.unique_io_id = new HashSet<Integer>();
			for(IOPoint p:q.ioSeq) {
				q.unique_io_id.add(p.ioID);
			}
		}
		if(q.recovery_io_id == null || q.recovery_io_id.isEmpty()) {
			q.recovery_io_id = new HashSet<Integer>();
			q.recovery_io_id.addAll(q.unique_io_id);
			q.recovery_io_id.removeAll(seedQ.unique_io_id);
		}
	}

	private void updateFuzzInfoInSaveIfInterestring(QueueEntry q, int faultMode, String testID, QueueEntry seedQ, long exec_seconds, int nb) {
		updateFuzzInfoInSaveIfInterestring(q, faultMode, testID, exec_seconds, nb);
	}

	private void updateFuzzInfoInSaveIfInterestring(QueueEntry q, int faultMode, String testID, long exec_seconds, int nb) {

		FuzzInfo.total_execs++;
		// FuzzInfo.exec_us += target.a_exec_seconds;
		FuzzInfo.exec_us += exec_seconds;

		if(faultMode != -1) {
			updateTimeToFaulsToTestsNum(q);
		}
		
		// int nb = coverage.has_new_bits();
		if(nb>0 && faultMode != -1) {
			FuzzInfo.lastNewCovFaults = q.faultSeq.seq.size();
			updateTimeToFaulsToNewCovTestsNum(q);
		}
		if (CoverageFilter.checkIfInteresting(faultMode, nb, q)) // imply faultMode == 0
		{
			Stat.log("*********************Test "+testID+" is ADDED to queue*********************");
		} else {
			if(faultMode >0) {
				if(faultMode == 1) {
					FuzzInfo.total_bugs++;
					Stat.log("*********************Find a BUG for test "+testID+"*********************");
					FuzzInfo.lastNewBugTime = FuzzInfo.getUsedSeconds();
					FuzzInfo.lastNewBugFaults = q.faultSeq.seq.size();
					updateTimeToFaulsBugsNum(q);
				} else if (faultMode == 2) {
					FuzzInfo.total_hangs++;
					Stat.log("*********************Find a HANG for test "+testID+"*********************");
				}
			} else if(faultMode <0) {
				Stat.log("*********************Test "+testID+" CANNOT be triggered*********************");
			}
		}
		FuzzInfo.total_bitmap_size += q.bitmap_size;
		FuzzInfo.total_bitmap_entries++;
	}

	private void writeToFileInSaveIfInterestring(QueueEntry q, int faultMode, String testID, String seedName, int nb, List<String> logInfo) {
		
		// FileUtil.generateFAVLogInfo(seedName, testID, target.logInfo, q.faultSeq);

		// int nb = coverage.has_new_bits();

		FileUtil.generateFAVLogInfo(seedName, testID, logInfo, q.faultSeq);

		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
		FileUtil.writeNeighborNewCovs(testID, q.faultSeq.adjacent_new_covs);
		FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);

		long usedSeconds = FuzzInfo.getUsedSeconds();
		if (CoverageFilter.checkIfInteresting(faultMode, nb, q)) // imply faultMode == 0
		{
			FileUtil.copyToQueue(testID, conf);
		} else {
			if(faultMode >0) {
				if(faultMode == 1) {
					FileUtil.copyDirToBugs(testID, usedSeconds);
				} else if (faultMode == 2) {
					FileUtil.copyDirToHangs(testID, usedSeconds);
				}
			} else if(faultMode <0) {
				FileUtil.copyToUntriggered(testID,conf);
				if(!Conf.DEBUG) {
					FileUtil.delete(FileUtil.root_tmp+testID);
				}
			}
		}
		FileUtil.copyToTested(testID, usedSeconds, conf);
		if(!Conf.DEBUG) {
			FileUtil.delete(FileUtil.root_tmp+testID);
		}
	}

	private void writeToFileInSaveIfInterestring(QueueEntry q, int faultMode, String testID, QueueEntry seedQ, int nb, List<String> logInfo) {
		String seedName = seedQ.fname;
		writeToFileInSaveIfInterestring(q, faultMode, testID, seedName, nb, logInfo);
	}

	public static class CoverageFilter {
		public static boolean checkIfInteresting(int faultMode, int nb, QueueEntry q) {
			boolean result = false;
			if (faultMode != 0) return false;
			if (nb>0) return true;
			result = q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH;
			// result = (faultMode == 0) && (nb>0 || q.faultSeq.seq.get(q.faultSeq.seq.size()-1).stat == FaultStat.CRASH);
			return result;
		}
	}

	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	/*
	 * Crashes and hangs are considered "unique" if the associated execution paths
	 * involve any state transitions not seen in previously-recorded faults. 
	 */

	public boolean save_if_interesting_rewrite(QueueEntry q, int faultMode, String testID, QueueEntry seedQ, long exec_seconds, List<String> logInfo) {
		boolean result = true;
		coverage.read_bitmap(FileUtil.root_tmp+testID+"/"+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		updateQInSaveIfInterestring(q, faultMode, testID, seedQ, nb, exec_seconds);
		if (CoverageFilter.checkIfInteresting(faultMode, nb, q)) {
			addToQueueAndMutateInSaveIfInterestring(q, testID, seedQ);
		}

		updateFuzzInfoInSaveIfInterestring(q, faultMode, testID, seedQ, exec_seconds, nb);
		writeToFileInSaveIfInterestring(q, faultMode, testID, seedQ, nb, logInfo);
		return result;
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

		

		q.handicap = 0;
		q.was_fuzzed = false;
		q.fuzzed_time = 0;
		  

		candidate_queue.add(q);

		// if (checkQueueEntrySuitedToReplay(q)) {
		// 	Stat.log("begin to record queue for replay!");
		// 	recoveryManager.recordQueue(q);
		// }
		
	}

	// public void collecIOSeqFromTrace(QueueEntry q, String fname) {
	// 	TraceReader reader = new TraceReader(FileUtil.root_tmp+fname+"/"+FileUtil.ioTracesDir);
	// 	reader.readTraces();
	// 	if(reader.ioPoints == null || reader.ioPoints.isEmpty()) {
	// 		return;
	// 	}
	// 	q.ioSeq = reader.ioPoints;
		
	// 	q.calibrate();
	// }
	
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

	public boolean checkQueueEntrySuitedToReplay(QueueEntry q) {
		boolean result = true;
		if (q.faultSeq.seq.size() <= 0) {
			return false;
		}
		if (q.ioSeq.indexOf(q.faultSeq.seq.get(0).ioPt) < 1) {
			return false;
		}
		return result;
	}

	public ArrayList<String> writeFAVENV(String favEnv) {
		if(conf.WRITE_FAV_ENV != null) {
			String path = conf.WRITE_FAV_ENV.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+favEnv, workingDir);
			//return RunCommand.run(path+" "+nodeName);
		} else {
			return null;
		}
	}

	public ArrayList<String> copyEnvToCluster() {
		if(conf.COPY_ENV_TO_CLUSTER != null) {
			String path = conf.COPY_ENV_TO_CLUSTER.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path+" "+nodeName);
		} else {
			return null;
		}
	}

	public ArrayList<String> copyLogsToController(String logsDir) {
		if (conf.COPY_LOGS_TO_CONTROLLER != null) {
			String path = conf.COPY_LOGS_TO_CONTROLLER.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path + " " + logsDir, workingDir);
		} else {
			return null;
		}
	}

	public ArrayList<String> copyLogsToControllerWithTestId(String testID) {
		ArrayList<String> result = null;
		result = copyLogsToController(conf.CLUSTER_LOGS_IN_CONTROLLER_DIR + "/" + testID);
		return result;
	}

	public void start() {
        FuzzInfo.total_execs = 0;

        // remove by fengwenhan
        // RecoveryManager recover = new RecoveryManager();
        // recover.loadQueue(candidate_queue, FileUtil.root_queue, conf);
        // recover.loadFuzzed(FuzzInfo.fuzzedFiles, FileUtil.root_fuzzed, conf);
		// Stat.log("Recovery message: ");
		// Stat.log(candidate_queue.size());
		// Stat.log(FuzzInfo.fuzzedFiles.size());
        
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

		if (conf.REPLAY_MODE) {
			replay();
		} else {
			if (!conf.RECOVERY_MODE) {
				writeFAVENV("fav-env-normal.sh");
				copyEnvToCluster();
				perform_first_run();
			} else {
				recovery();
			}
			writeFAVENV("fav-env-determine.sh");
			copyEnvToCluster();
			performOtherRun();
		}

		
        
        System.out.println(FuzzInfo.generateClientReport());
    }

	public QueueEntry retriveReplayQueueEntryFromJSONFilePath(String filepath) {
		QueueEntry result = null;
		File file = new File(filepath);
		List<String> oriList;
		try {
			oriList = Files.readAllLines(file.toPath());
			String s = oriList.get(0);
			QueueEntry entry = JSON.parseObject(s, QueueEntry.class);
			// Stat.log(JSONObject.toJSONString(entry));
			result = entry;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public QueueEntry retriveReplayQueueEntryFromRSTFolder(String filepath) {
		TraceReader tr = new TraceReader(filepath + "/fav-rst");
        tr.readTraces();
		tr.fixWROrderInSameTimeStamp(tr.ioPoints);
        QueueEntry e = new QueueEntry();
        e.ioSeq = tr.ioPoints;
        FaultSequence faultSeq = FileUtil.loadCurrentCrashPoint(filepath + "/zk363curCrash");
		if (faultSeq == null) {
			faultSeq = new FaultSequence();
			// faultSeq.seq.add(null);
		}
		e.faultSeq = faultSeq;
		return e;
	}

	

	

	public void replay(String filepath) {
		// QueueEntry entry = retriveReplayQueueEntryFromJSONFilePath(filepath);
		// QueueEntry entry = retriveReplayQueueEntryFromRSTFolder("/data/fengwenhan/data/crashfuzz_ctrl/queue/10_2f");
		QueueEntry entry = retriveReplayQueueEntryFromRSTFolder(conf.REPLAY_TRACE_PATH);
		ReplayTarget rt = new ReplayTarget();
		// rt.replayATest(entry, conf, "replay", conf.hangSeconds);
		// rt.replayATest(entry, conf, "replay", conf.REPLAY_HANG_TIME);
		FaultSeqAndIOSeq seqPair = new FaultSeqAndIOSeq(entry.faultSeq, entry.ioSeq);
		rt.beforeTarget(seqPair, conf, "replay", conf.REPLAY_HANG_TIME);
		rt.doTarget();
		int result = rt.afterTarget().result;
		Stat.log("replay result: " + result);
	}

	public void replay() {
		replay(conf.REPLAY_QUEUEENTRY_PATH);
	}

	public void recovery() {
		Stat.log("recoveryFuzzInfo...");
		recoveryManager.recoverFuzzInfo(this);
		Stat.log("recoveryCandidateQueue...");
		recoveryManager.recoverCandidateQueue(this);
		Stat.log("recoveryTestedFaultId...");
		recoveryManager.recoverTestedFaultId(this);
		Stat.log("recoveryVirginBits...");
		recoveryManager.recoverVirginBits(this);
	}

	public void performOtherRun() {
		if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }

        boolean hasFaultSequence = true;
        while (( FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes*60)) && hasFaultSequence) {
        	queue_cycle++;

        	cull_queue();

        	// QueuePair q = QueueManagerNew.retrieveAnEntry(candidate_queue);
        	// if(q == null) {
        	// 	break;
        	// }
			// doARun(q);

			List<QueuePair> pairList = QueueManagerNew.retrievePairListFWH(candidate_queue, conf);
			for (QueuePair pair : pairList) {
				doARun(pair);
			}

			
        	// Stat.log("Going to test queue entry"+q.seedIdx+"'s mutation:"+q.mutateIdx);
        	
        	// int exec_rst = common_fuzz_stuff(q.mutate, q.seed);
        	
        	// q.seed.was_fuzzed = true;
        	// FileUtil.updateQueueInfo(q.seed.fname, q.seed.mutates, q.seed.fuzzed_time, q.seed.handicap);
        	
        	// if(exec_rst != -1) {
            // 	update_queue(q);
        	// }

        	// if(Conf.MANUAL) {
    		// 	Scanner scan = new Scanner(System.in);
            // 	scan.nextLine();
    		// }
//        	fuzzed_queue.add(q);
        	
        	hasFaultSequence = !candidate_queue.isEmpty();

			recoveryManager.recordFuzzInfo(this);
			recoveryManager.recordCandidateQueue(this);
			recoveryManager.recordTestedFaultId(this);
			recoveryManager.recordVirginBits(this);

			
        }
	}

	public void doARun(QueuePair q) {
		
		Stat.log("Going to test queue entry" + q.seedIdx + "'s mutation:" + q.mutateIdx);
		int exec_rst = common_fuzz_stuff(q.mutate, q.seed);
		q.seed.was_fuzzed = true;
		FileUtil.updateQueueInfo(q.seed.fname, q.seed.mutates, q.seed.fuzzed_time, q.seed.handicap);
		if (exec_rst != -1) {
			update_queue(q);
		}
		if (Conf.MANUAL) {
			Scanner scan = new Scanner(System.in);
			scan.nextLine();
		}
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
