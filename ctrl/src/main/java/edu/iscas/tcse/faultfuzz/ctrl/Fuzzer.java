package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import edu.iscas.tcse.faultfuzz.ctrl.Conf.EVALUATE_TARGET_SET;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractDeterminismTarget.FaultSeqAndIOSeq;
import edu.iscas.tcse.faultfuzz.ctrl.control.NormalTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.determine.TryBestDeterminismTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.determine.TryBestDeterminismTarget.TryBestDeterminismTResult;
import edu.iscas.tcse.faultfuzz.ctrl.filter.CoverageGuidedFilter;
import edu.iscas.tcse.faultfuzz.ctrl.filter.EnumerationFilter;
import edu.iscas.tcse.faultfuzz.ctrl.selection.FIFOQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo.QueuePair;
import edu.iscas.tcse.faultfuzz.ctrl.selection.score.ScoreQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

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

	FaultSequenceConstructor constructor;

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
		constructor = new FaultSequenceConstructor();
    }

    public static long getExecSeconds(long start) {
        return (((System.currentTimeMillis()-start)/ 1000));
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
		

		// NormalTarget target = new NormalTarget();
		// int rst = -1;
		// rst = target.run_target(empty, conf, "init", conf.hangSeconds);

		TryBestDeterminismTarget target = new TryBestDeterminismTarget();
		FaultSeqAndIOSeq faultSeqAndIOSeq = new FaultSeqAndIOSeq(q.faultSeq, q.ioSeq);
		target.beforeTarget(faultSeqAndIOSeq, conf, "init", conf.hangSeconds);
		target.doTarget();
		TryBestDeterminismTResult tbdResult = target.afterTarget();
		int rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.resultCode);

		String tmpRootDir = monitor.getTmpReportDir(testID);
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();

		updateQInSaveIfInterestring(q, rst, testID, nb, target.a_exec_seconds);

		Stat.debug("Begin to collect runtime IO information...");
		String tracePath = FileUtil.root_tmp + testID + "/" + FileUtil.ioTracesDir;
		Stat.debug("tracePath is " + tracePath);
		TraceReader tr = new TraceReader(FileUtil.root_tmp + testID + "/" + FileUtil.ioTracesDir);
		tr.readTraces();
		List<IOPoint> unorderedIOPoints = tr.ioPoints;
		Stat.debug("unorderedIOPoints IO size is" + unorderedIOPoints.size());
		List<FaultPoint> injectedFaultPointList = tbdResult.injectedFaultPointList;
		Stat.debug("Begin to construct fault sequence...");
		constructor.constructQueueEntry(q, unorderedIOPoints, injectedFaultPointList);
		// Stat.debug("q is after constructed! q is: " + JSONObject.toJSONString(q, SerializerFeature.IgnoreNonFieldGetter));
		Stat.debug("q is after constructed!" );
		
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
			Mutation.mutateFaultSequence(q, conf);
			totalSeedCases++;

			if (q.mutates == null || q.mutates.size() == 0) {
				candidate_queue.remove(q);
			}
		}
		Stat.debug("q is after mutation! ");
		// Stat.debug("q is after mutation! q is: " + JSONObject.toJSONString(q, SerializerFeature.IgnoreNonFieldGetter));

		updateFuzzInfoInSaveIfInterestring(q, rst, testID, target.a_exec_seconds, nb);

		writeToTmpFileInSaveIfInterestring(q, rst, testID, "", nb, target.logInfo);
		copyFileToCorrespondingDirByFaultMode(q, rst, testID, "", nb, target.logInfo);
		
		if(Conf.MANUAL) {
			Scanner scan = new Scanner(System.in);
        	scan.nextLine();
		}
		if(!Conf.DEBUG) {
			// FileUtil.delete(tmpRootDir);
		}

        recordGlobalInfo();

		if(q == null || q.mutates.isEmpty()) {
    		int seedIdx = candidate_queue.indexOf(q);
			candidate_queue.remove(seedIdx);
        	FileUtil.removeFromQueue(q.fname, conf);
        	FuzzInfo.fuzzedFiles.add(q.fname);
        	FileUtil.copyToFuzzed(q.fname, FuzzInfo.getUsedSeconds());
    	}
		
	}

	

	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueuePair pair) {
		//save current test case to file
		//run_target
		//save_if_interesting
		// String fname = stat.saveTestCase();
		QueueEntry q = pair.mutate ;
		QueueEntry seedQ = pair.seed;
		
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
		rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.resultCode);

		// NormalTarget target = new NormalTarget();
		// rst = target.run_target(q.faultSeq, conf, testID, waitTime);

		q.fname = testID;
		q.was_tested = true;
		
		pair.seed.was_fuzzed = true;
		FileUtil.updateQueueInfo(pair.seed.fname, pair.seed.mutates, pair.seed.fuzzed_time, pair.seed.handicap);
		if (rst != -1) {
			updateMetricOfQueuePair(pair);
		}
		
		removeMutateFromSeedAndCandidateQueue(pair);
		
        // save_if_interesting(q, rst, q.fname, seedQ);
		// save_if_interesting_rewrite(q, rst, testID, seedQ, tbdResult.exec_time, tbdResult.logInfo);
		save_if_interesting_rewrite(pair, rst, testID, tbdResult);
        

        if(rst == 2) {//test again for hang cases with a larger timeout
			/**
			 * To fix bug, comment this hang test temporarily
			 */
        	// rst = testAgainForHang(pair, waitTime, testID, rst, target, faultSeqAndIOSeq, start);
        }
        
        FuzzInfo.testedUniqueCases.add(q.faultSeq.getFaultSeqID());
        
        recordGlobalInfo();

        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
        
		return rst;
	}

	private int testAgainForHang(QueuePair pair, long waitTime, String testID, int rst,
			TryBestDeterminismTarget target, FaultSeqAndIOSeq faultSeqAndIOSeq, long start) {
		QueueEntry q = pair.mutate ;
		QueueEntry seedQ = pair.seed;
		TryBestDeterminismTResult tbdResult;
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
		rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.resultCode);


		q.fname = testID+"-retry";

		if(lastRst == 2 && rst != 2){
			FuzzInfo.total_hangs--;
			FileUtil.removeFromHang(testID,conf);
		}
		if (lastRst == 2 && rst == 2) {
			FuzzInfo.lastNewHangTime = FuzzInfo.getUsedSeconds();
			FuzzInfo.lastNewHangFaults = q.faultSeq.seq.size();
			FuzzInfo.updateTimeToFaulsHangsNum(q);
		}

		// save_if_interesting(q, rst, q.fname, seedQ);
		// save_if_interesting_rewrite(q, rst, q.fname, seedQ, target);
		// save_if_interesting_rewrite(q, rst, testID, seedQ, tbdResult.exec_time, tbdResult.logInfo);
		save_if_interesting_rewrite(pair, rst, testID, tbdResult);
		return rst;
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
		Mutation.mutateFaultSequence(q, conf);
		if (q.mutates == null || q.mutates.size() == 0) {
			candidate_queue.remove(q);
		}
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
			FuzzInfo.updateTimeToFaulsToTestsNum(q);
		}
		
		// int nb = coverage.has_new_bits();
		if(nb>0 && faultMode != -1) {
			FuzzInfo.lastNewCovFaults = q.faultSeq.seq.size();
			FuzzInfo.updateTimeToFaulsToNewCovTestsNum(q);
		}
		if (CoverageGuidedFilter.checkIfInteresting(faultMode, nb, q)) // imply faultMode == 0
		{
			Stat.log("*********************Test "+testID+" is ADDED to queue*********************");
		} else {
			if(faultMode >0) {
				if(faultMode == 1) {
					FuzzInfo.total_bugs++;
					Stat.log("*********************Find a BUG for test "+testID+"*********************");
					FuzzInfo.lastNewBugTime = FuzzInfo.getUsedSeconds();
					FuzzInfo.lastNewBugFaults = q.faultSeq.seq.size();
					FuzzInfo.updateTimeToFaulsBugsNum(q);
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

	private void writeToTmpFileInSaveIfInterestring(QueueEntry q, int faultMode, String testID, String seedName, int nb, List<String> logInfo) {
		
		// FileUtil.generateFAVLogInfo(seedName, testID, target.logInfo, q.faultSeq);
		// int nb = coverage.has_new_bits();

		FileUtil.generateFAVLogInfo(seedName, testID, logInfo, q.faultSeq);

		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
		FileUtil.writeNeighborNewCovs(testID, q.faultSeq.adjacent_new_covs);
		FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);
		FileUtil.writeFaultSeq(testID, q.faultSeq);
		FileUtil.writeFaultJSONSeq(testID, q.faultSeq);

		// Stat.debug("copy cur_crash " + conf.CUR_FAULT_FILE.getAbsolutePath() + " to root_tmp: " + FileUtil.root_tmp+testID);
		// FileUtil.copyFileToDir(conf.CUR_FAULT_FILE.getAbsolutePath(), FileUtil.root_tmp+testID);

		// File tmpDir = new File(FileUtil.root_tmp+testID);
		// String fileNameInfo = "";
		// for (File file : tmpDir.listFiles()) {
		// 	fileNameInfo += file.getName() + ", ";
        // }
		// Stat.debug("tmpDir: [" + fileNameInfo + "]");
		
	}

	private void copyFileToCorrespondingDirByFaultMode(QueueEntry q, int faultMode, String testID, String seedName, int nb, List<String> logInfo) {
		long usedSeconds = FuzzInfo.getUsedSeconds();
		if (CoverageGuidedFilter.checkIfInteresting(faultMode, nb, q)) // imply faultMode == 0
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
	}



	

	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	/*
	 * Crashes and hangs are considered "unique" if the associated execution paths
	 * involve any state transitions not seen in previously-recorded faults. 
	 */

	public boolean save_if_interesting_rewrite(QueuePair pair, int faultMode, String testID, TryBestDeterminismTResult targetResult) {

		QueueEntry q = pair.mutate;
		QueueEntry seedQ = pair.seed;

		// boolean result = save_if_interesting_rewrite(q, faultMode, testID, seedQ, targetResult.exec_time, targetResult.logInfo);
		long exec_seconds = targetResult.exec_time;
		List<String> logInfo = targetResult.logInfo;
		boolean result = true;
		coverage.read_bitmap(FileUtil.root_tmp+testID+"/"+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();

		

		updateQInSaveIfInterestring(q, faultMode, testID, seedQ, nb, exec_seconds);

		Stat.log("Begin to collect runtime IO information...");
		TraceReader tr = new TraceReader(FileUtil.root_tmp + testID + "/" + FileUtil.ioTracesDir);
		tr.readTraces();
		List<IOPoint> unorderedIOPoints = tr.ioPoints;
		List<FaultPoint> injectedFaultPointList = targetResult.injectedFaultPointList;

		Stat.log("Begin to construct fault sequence...");
		/**
		 * We reuse q to save memory.
		 */
		constructor.constructQueueEntry(q, unorderedIOPoints, injectedFaultPointList);
		Stat.debug("After construction, the fault sequence is: " + JSONObject.toJSONString(q.faultSeq, SerializerFeature.IgnoreNonFieldGetter));
		
		boolean isInteresting = false;
		if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.FaultFuzzer || conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.CrashFuzzerMinus) {
			Stat.log("use CoverageFilter to checkIfInteresting...");
			isInteresting = CoverageGuidedFilter.checkIfInteresting(faultMode, nb, q);
		} 
		if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.BruteForce) {
			Stat.log("use CrashFuzzerMinusFilter to checkIfInteresting...");
			isInteresting = EnumerationFilter.checkIfInteresting(faultMode, nb, q);
		}
		Stat.log("checkIfInteresting result is: " + isInteresting);

		if (isInteresting) {
			Stat.log("Begin to mutate...");
			addToQueueAndMutateInSaveIfInterestring(q, testID, seedQ);
		}

		Stat.log("Record evaluation data...");
		updateFuzzInfoInSaveIfInterestring(q, faultMode, testID, seedQ, exec_seconds, nb);
		// writeToFileInSaveIfInterestring(q, faultMode, testID, seedQ, nb, logInfo);
		String seedName = seedQ.fname;
		writeToTmpFileInSaveIfInterestring(q, faultMode, testID, seedName, nb, logInfo);
		copyFileToCorrespondingDirByFaultMode(q, faultMode, testID, seedName, nb, logInfo);
		if(!Conf.DEBUG) {
			FileUtil.delete(FileUtil.root_tmp+testID);
		}
		return result;
	}

	/* Append new test case to the queue. */
	public void add_to_queue(QueueEntry q, String fname) {
		q.handicap = 0;
		q.was_fuzzed = false;
		q.fuzzed_time = 0;

		candidate_queue.add(q);
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
			Replayer replayer = new Replayer(conf);
			QueueEntry entry = replayer.retriveReplayQueueEntryFromRSTFolder(conf.REPLAY_TRACE_PATH);
			replayer.replay(entry);
		} else {
			if (!conf.RECOVERY_MODE) {
				// writeFAVENV("fav-env-normal.sh");
				perform_first_run();
			} else {
				recovery();
			}
			// writeFAVENV("fav-env-determine.sh");
			performOtherRun();
		}

		
        
        System.out.println(FuzzInfo.generateClientReport());
		
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

			// List<QueuePair> pairList = QueueManagerNew.retrievePairListInTranditionFuzzingProcess(candidate_queue, conf);

			List<QueuePair> pairList = null;
			if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.BruteForce || conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.CrashFuzzerMinus) {
				pairList = FIFOQueueEntrySelector.retrieveAPairList(candidate_queue, conf);
			}
			if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.FaultFuzzer) {
				pairList = ScoreQueueEntrySelector.retrieveAPairList(candidate_queue, conf);
			}
			
			// List<QueuePair> pairList = QueueManagerBruteForce.retrieveAPairList(candidate_queue, conf);
			for (SelectionInfo.QueuePair pair : pairList) {
				doARun(pair);
			}

			// QueuePair pair = QueueManagerNew.retrieveAnEntry(candidate_queue);
			// doARun(pair);

			
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
			Stat.debug(this.getClass(), "candidate_queue size is: " + candidate_queue.size());
			Stat.debug(this.getClass(), "hasFaultSequence is: " + hasFaultSequence);

			// recoveryManager.recordFuzzInfo(this);
			// recoveryManager.recordCandidateQueue(this);
			// recoveryManager.recordTestedFaultId(this);
			// recoveryManager.recordVirginBits(this);

			
        }
	}

	public void doARun(SelectionInfo.QueuePair pair) {
		Stat.log("Going to test queue entry " + pair.seedIdx + "'s mutation:" + pair.mutateIdx);
		int exec_rst = common_fuzz_stuff(pair);
		
	}

	// public updateAndRemoveSeedAndMutate() {

	// }

	/* Take the current entry from the queue, fuzz it for a while. This
	   function is a tad too long... returns 0 if fuzzed successfully, 1 if
	   skipped or bailed out. */

	   public void updateMetricOfQueuePair(SelectionInfo.QueuePair pair) {

		// q.seed.faultPointsToMutate.remove(q.mutateIdx);
    	// q.seed.mutates.remove(q.mutateIdx);

		QueueEntry seed = pair.seed;
		QueueEntry mutate = pair.mutate;

		if(mutate.faultSeq.on_recovery) {
    		seed.on_recovery_mutates.remove(mutate);
    	}
    	
    	FaultPoint tmpLastFault = mutate.faultSeq.seq.get(mutate.faultSeq.seq.size()-1);
		int tmpID = tmpLastFault.getFaultID();
		seed.not_tested_fault_id.remove(tmpID);
		SelectionInfo.tested_fault_id.add(tmpID);

		SelectionInfo.testedFault.add(tmpLastFault);
    	
		FaultPoint injected_fault = tmpLastFault;
		if(mutate.favored) {
    		seed.favored_mutates.remove(mutate);
    	}
		updateFavorListOfPairSeed(pair, injected_fault);
	}

	private void removeMutateFromSeedAndCandidateQueue(QueuePair pair) {
		QueueEntry seed = pair.seed;
		QueueEntry mutate = pair.mutate;
		int mIndex = seed.mutates.indexOf(mutate);
		seed.faultPointsToMutate.remove(mIndex);
		seed.mutates.remove(mIndex);

		if(seed.mutates == null || seed.mutates.isEmpty()) {
    		int seedIdx = pair.seedIdx;
			candidate_queue.remove(seedIdx);
        	FileUtil.removeFromQueue(seed.fname, conf);
        	FuzzInfo.fuzzedFiles.add(seed.fname);
        	FileUtil.copyToFuzzed(seed.fname, FuzzInfo.getUsedSeconds());
    	}
	}

	private void updateFavorListOfPairSeed(SelectionInfo.QueuePair q, FaultPoint injected_fault) {
		int mutateIdx = q.mutateIdx;
		int startLoc = (mutateIdx-10)>=0? q.mutateIdx-10:0;
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
