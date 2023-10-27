package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import edu.iscas.tcse.faultfuzz.ctrl.Conf.EVALUATE_TARGET_SET;
import edu.iscas.tcse.faultfuzz.ctrl.control.determine.TryBestDeterminismTarget;
import edu.iscas.tcse.faultfuzz.ctrl.control.determine.TryBestDeterminismTarget.TryBestDeterminismTResult;
import edu.iscas.tcse.faultfuzz.ctrl.filter.CoverageGuidedFilter;
import edu.iscas.tcse.faultfuzz.ctrl.filter.EnumerationFilter;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.report.BeautifulReport;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;
import edu.iscas.tcse.faultfuzz.ctrl.selection.FIFOQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo.QueuePair;
import edu.iscas.tcse.faultfuzz.ctrl.selection.score.ScoreQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class Fuzzer {
	public static int MAP_SIZE = 100;
    Conf conf;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    public static boolean running = false;
    
    public static List<QueueEntry> candidate_queue;
    
	long handicap;
	long depth;

	JSONBasedRecoveryManager recoveryManager;

	EntryConstructor constructor;

    public Fuzzer(Conf conf) {
    	// monitor = new Monitor(conf);
		monitor = new Monitor(conf.MONITOR.getAbsolutePath());
    	stat = new Stat();
    	this.conf = conf;
    	coverage = new CoverageCollector();
    	candidate_queue = new ArrayList<QueueEntry>();

		recoveryManager = new JSONBasedRecoveryManager();
		constructor = new EntryConstructor();
    }

    public static long getExecSeconds(long start) {
        return (((System.currentTimeMillis()-start)/ 1000));
    }

	private void updateQueueEntryInSaveIfInterestring(QueueEntry entry, long exec_s) {
		entry.bitmap_size = CoverageCollector.coveredBlocks(CoverageCollector.trace_bits);
		entry.exec_s = exec_s;
	}

	public void updateQueueEntryRuntimeInfo(QueueEntry entry) {
		entry.handicap = 0;
		// entry.fuzzed_time = 0;
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
		FileUtil.generateFAVLogInfo(seedName, testID, logInfo, q.faultSeq);
		FileUtil.writeMap(testID, CoverageCollector.trace_bits, FuzzInfo.getTotalCoverage(CoverageCollector.trace_bits), nb);
		FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);
		FileUtil.writeFaultSeq(testID, q.faultSeq);
		FileUtil.writeFaultJSONSeq(testID, q.faultSeq);
	}

	private void copyFileToCorrespondingDirByFaultMode(QueueEntry q, int faultMode, String testID, String seedName, int nb, List<String> logInfo) {
		long usedSeconds = FuzzInfo.getUsedSeconds();
		if (CoverageGuidedFilter.checkIfInteresting(faultMode, nb, q)) // imply faultMode == 0
		{
			FileUtil.copyToQueue(testID, conf.CUR_FAULT_FILE.getName());
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
		FileUtil.copyToTested(testID, usedSeconds, conf.CUR_FAULT_FILE.getName());

		FileUtil.copyDirToPersist(testID);

	}



	public void performNoFaultRun(QueueEntry entry) {
		TryBestDeterminismTarget target = new TryBestDeterminismTarget();
		QueueEntryRuntime entryRuntime = new QueueEntryRuntime(entry);
		Cluster cluster = new Cluster(conf);
		target.beforeTarget(entryRuntime, cluster, conf.AFL_PORT, conf.CUR_FAULT_FILE, conf.MONITOR, entry.fname, conf.hangSeconds, conf.CONTROLLER_PORT, conf.maxDownGroup, conf.DETERMINE_WAIT_TIME);
		target.doTarget();
		TryBestDeterminismTResult tbdResult = target.afterTarget();
		int rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.resultCode);

		String tmpRootDir = monitor.getTmpReportDir(entry.fname);
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();

		updateQueueEntryInSaveIfInterestring(entry, target.a_exec_seconds);

		Stat.debug("Begin to collect runtime IO information...");
		String tracePath = FileUtil.root_tmp + entry.fname + "/" + FileUtil.ioTracesDir;
		Stat.debug("tracePath is " + tracePath);
		TraceReader tr = new TraceReader(FileUtil.root_tmp + entry.fname + "/" + FileUtil.ioTracesDir);
		tr.readTraces();
		List<IOPoint> unorderedIOPoints = tr.ioPoints;
		Stat.debug("unorderedIOPoints IO size is" + unorderedIOPoints.size());
		List<FaultPoint> injectedFaultPointList = tbdResult.injectedFaultPointList;
		Stat.debug("Begin to construct fault sequence...");
		constructor.constructQueueEntry(entry, unorderedIOPoints, injectedFaultPointList);
		// Stat.debug("q is after constructed! q is: " + JSONObject.toJSONString(q, SerializerFeature.IgnoreNonFieldGetter));
		Stat.debug("q is after constructed!" );
		
		/*
		 * Even if the fault sequence does not trigger new basic blocks,
		 * we mutate it since it is the empty fault sequence of another workload.
		 */
		// candidate_queue.add(entry);
		// updateQueueEntryRuntimeInfo(entry);
		// initializeUniqueIOId(entry);
		// if (entry.recovery_io_id == null || entry.recovery_io_id.isEmpty()) {
		// 	entry.recovery_io_id = new HashSet<Integer>();
		// }
		// Mutation.initializeFaultPointsToMutate(entry, conf.MAX_FAULTS, conf.maxDownGroup);
		// Mutation.initializeLocalNotTestedFaultId(entry);
		// Mutation.mutateFaultSequence(entry);
		// if (entry.mutates == null || entry.mutates.size() == 0) {
		// 	candidate_queue.remove(entry);
		// }

		addToQueueAndMutateInSaveIfInterestring(entry, null);
		
		Stat.debug("q is after mutation! ");
		// Stat.debug("q is after mutation! q is: " + JSONObject.toJSONString(q, SerializerFeature.IgnoreNonFieldGetter));

		updateFuzzInfoInSaveIfInterestring(entry, rst, entry.fname, target.a_exec_seconds, nb);

		writeToTmpFileInSaveIfInterestring(entry, rst, entry.fname, "", nb, target.logInfo);
		copyFileToCorrespondingDirByFaultMode(entry, rst, entry.fname, "", nb, target.logInfo);
		
        recordGlobalInfo();

		if(entry == null || entry.mutates.isEmpty()) {
    		int seedIdx = candidate_queue.indexOf(entry);
			candidate_queue.remove(seedIdx);
        	FileUtil.removeFromQueue(entry.fname, conf);
    	}
	}

	public void performNoFaultRun(int workloadIndex) {
		File workload = Conf.WORKLOADLIST.get(workloadIndex);
		
		Stat.log("***********************Perform inital runs to collect IO traces*****************************");
		String testID = "init-" + FuzzInfo.total_execs;
		FaultSequence empty = FaultSequence.createAnEmptyFaultSequence();
		QueueEntry entry = new QueueEntry();
		entry.faultSeq = empty;
		entry.fname = testID;
		entry.ioSeq = new ArrayList<>();
		entry.workload = workload;

		Conf.currentWorkload = workload;

		performNoFaultRun(entry);	
	}

	//0 triggered, no bug
	//1 triggered, non-hang bug
	//2 triggered, hang bug
	//-1 not triggered
	/**
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

		// updateQInSaveIfInterestring(q, faultMode, testID, seedQ, nb, exec_seconds);
		updateQueueEntryInSaveIfInterestring(seedQ, exec_seconds);

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
			addToQueueAndMutateInSaveIfInterestring(q, seedQ);
		}

		Stat.log("Record evaluation data...");
		// updateFuzzInfoInSaveIfInterestring(q, faultMode, testID, seedQ, exec_seconds, nb);
		updateFuzzInfoInSaveIfInterestring(q, faultMode, testID, exec_seconds, nb);
		// writeToFileInSaveIfInterestring(q, faultMode, testID, seedQ, nb, logInfo);
		String seedName = seedQ.fname;
		writeToTmpFileInSaveIfInterestring(q, faultMode, testID, seedName, nb, logInfo);
		copyFileToCorrespondingDirByFaultMode(q, faultMode, testID, seedName, nb, logInfo);
		if(!Conf.DEBUG) {
			FileUtil.delete(FileUtil.root_tmp+testID);
		}
		return result;
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
    	}
	}
	
	/* Write a modified test case, run program, process results. Handle
	   error conditions, returning 1 if it's time to bail out. This is
	   a helper function for fuzz_one(). */
	public int common_fuzz_stuff(QueuePair pair) {
		QueueEntry q = pair.mutate ;
        
        long waitTime = conf.hangSeconds > q.exec_s*3? conf.hangSeconds : q.exec_s*3;	
        String testID = String.valueOf(FuzzInfo.total_execs+1)+"_"+q.faultSeq.seq.size()+"f";
		int rst = -1;

		Conf.currentWorkload = q.workload;

		TryBestDeterminismTarget target = new TryBestDeterminismTarget();
		QueueEntryRuntime entryRuntime = new QueueEntryRuntime(q);
		Cluster cluster = new Cluster(conf);
		target.beforeTarget(entryRuntime, cluster, conf.AFL_PORT, conf.CUR_FAULT_FILE, conf.MONITOR, testID, waitTime, conf.CONTROLLER_PORT, conf.maxDownGroup, conf.DETERMINE_WAIT_TIME);
		target.doTarget();
		TryBestDeterminismTResult tbdResult = target.afterTarget();
		rst = TryBestDeterminismTarget.mapTBDResutToFaultMode(tbdResult.resultCode);

		// NormalTarget target = new NormalTarget();
		// rst = target.run_target(q.faultSeq, conf, testID, waitTime);

		q.fname = testID;
		
		FileUtil.updateQueueInfo(pair.seed.fname, pair.seed.mutates, pair.seed.handicap);
		if (rst != -1) {
			updateMetricOfQueuePair(pair);
		}
		
		removeMutateFromSeedAndCandidateQueue(pair);
		
		save_if_interesting_rewrite(pair, rst, testID, tbdResult);
        
        if(rst == 2) {//test again for hang cases with a larger timeout
			/**
			 * To fix bug, comment this hang test temporarily
			 */
        	// rst = testAgainForHang(pair, waitTime, testID, rst, target, faultSeqAndIOSeq, start);
        }
        
        FuzzInfo.testedUniqueCases.add(q.faultSeq.getFaultSeqID());
        
        recordGlobalInfo();
		return rst;
	}

	private int testAgainForHang(QueuePair pair, long waitTime, String testID, int rst,
			TryBestDeterminismTarget target, QueueEntryRuntime faultSeqAndIOSeq, long start) {
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
		Cluster cluster = new Cluster(conf);
		target.beforeTarget(faultSeqAndIOSeq, cluster, conf.AFL_PORT, conf.CUR_FAULT_FILE, conf.MONITOR, testID+"-retry", conf.hangSeconds*2, conf.CONTROLLER_PORT, conf.maxDownGroup, conf.DETERMINE_WAIT_TIME);
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
	
	public void initializeUniqueIOId(QueueEntry entry) {
		// if(entry.unique_io_id == null || entry.unique_io_id.isEmpty()) {
		// 	entry.unique_io_id = new HashSet<Integer>();
		// 	for(IOPoint p:entry.ioSeq) {
		// 		entry.unique_io_id.add(p.ioID);
		// 	}
		// }
	}

	public void initializeRecoveryIOIdWithUniqueIOId(QueueEntry entry, QueueEntry seedQ) {

		Set<Integer> recoveryIOId = getRecoveryIOId(entry, seedQ);
		entry.recovery_io_id = recoveryIOId;
		
		// initializeUniqueIOId(entry);
		// if(entry.recovery_io_id == null || entry.recovery_io_id.isEmpty()) {
		// 	entry.recovery_io_id = new HashSet<Integer>();
		// 	// for init entry, we call this function with seedQ is null.
		// 	if (seedQ != null) {
		// 		entry.recovery_io_id.addAll(entry.unique_io_id);
		// 		entry.recovery_io_id.removeAll(seedQ.unique_io_id);
		// 	}
			
		// }
	}

	public static Set<Integer> getRecoveryIOId(QueueEntry entry, QueueEntry seedQ) {
		// handle init entry.
		if (seedQ == null || seedQ.ioSeq == null || seedQ.ioSeq.isEmpty()) {
			return new HashSet<>();
		}
		Set<Integer> entryUniqueIOId = new HashSet<Integer>();
		for(IOPoint p:entry.ioSeq) {
			entryUniqueIOId.add(p.ioID);
		}
		Set<Integer> seedUniqueIOId = new HashSet<Integer>();
		for(IOPoint p:seedQ.ioSeq) {
			seedUniqueIOId.add(p.ioID);
		}
		Set<Integer> recoveryIOId = new HashSet<Integer>();
		recoveryIOId.addAll(entryUniqueIOId);
		recoveryIOId.removeAll(seedUniqueIOId);
		return recoveryIOId;
	}

	private void addToQueueAndMutateInSaveIfInterestring(QueueEntry entry, QueueEntry seedQ) {

		
		updateQueueEntryRuntimeInfo(entry);
		initializeRecoveryIOIdWithUniqueIOId(entry, seedQ);
		Mutation.initializeFaultPointsToMutate(entry, conf.MAX_FAULTS, conf.maxDownGroup);
		Mutation.initializeLocalNotTestedFaultId(entry);
		Mutation.mutateFaultSequence(entry);
		// candidate_queue.add(entry);
		// if (entry.mutates == null || entry.mutates.size() == 0) {
		// 	candidate_queue.remove(entry);
		// }
		if (entry.mutates != null && entry.mutates.size() > 0) {
			candidate_queue.add(entry);
		}
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
	

	public void doARun(SelectionInfo.QueuePair pair) {
		Stat.log("Going to test queue entry " + pair.seedIdx + "'s mutation:" + pair.mutateIdx);
		int exec_rst = common_fuzz_stuff(pair);
		
	}

	public void performOtherRun() {
		if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }

        boolean hasFaultSequence = true;
        while (( FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes*60)) && hasFaultSequence) {
        	cull_queue();
			List<QueuePair> pairList = null;
			if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.BruteForce || conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.CrashFuzzerMinus) {
				pairList = FIFOQueueEntrySelector.retrieveAPairList(candidate_queue, conf);
			}
			if (conf.EVALUATE_TARGET == EVALUATE_TARGET_SET.FaultFuzzer) {
				pairList = ScoreQueueEntrySelector.retrieveAPairList(candidate_queue, conf);
			}
			for (SelectionInfo.QueuePair pair : pairList) {

				while (checkPause()) {
					try {
						Thread.sleep(1000);
						Stat.log("FaultFuzzer is paused, waiting for resume...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				doARun(pair);
			}
        	
        	hasFaultSequence = !candidate_queue.isEmpty();
			Stat.debug(this.getClass(), "candidate_queue size is: " + candidate_queue.size());
			Stat.debug(this.getClass(), "hasFaultSequence is: " + hasFaultSequence);

			// recoveryManager.recordFuzzInfo(this);
			// recoveryManager.recordCandidateQueue(this);
			// recoveryManager.recordTestedFaultId(this);
			// recoveryManager.recordVirginBits(this);
        }
	}

	public void start() {
        FuzzInfo.total_execs = 0;
        FuzzInfo.startTime = System.currentTimeMillis();

        Thread observer = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					while ((FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes * 60))) {
						if (!checkPause()) {
							FileOutputStream out = new FileOutputStream(FileUtil.root + FileUtil.report_file);
							String report = BeautifulReport.generateBeautifulReportWithFuzzInfo();
							// String report = FuzzInfo.generateClientReport();
							out.write(report.getBytes());
							out.flush();
							out.close();
						}
						Thread.currentThread().sleep(1000);
					}
					// System.out.println(FuzzInfo.generateBeautifulReport());
					// System.out.println(FuzzInfo.generateClientReport());
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

		if (!conf.RECOVERY_MODE) {
			// perform_first_run();
			FileUtil.clearRootPath();
			for (int i = 0; i < Conf.WORKLOADLIST.size(); i++) {

				while (checkPause()) {
					try {
						Thread.sleep(1000);
						FuzzInfo.pauseSecond++;
						Stat.log("FaultFuzzer is paused, waiting for resume...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				performNoFaultRun(i);
			}
		} else {
			recovery();
		}
		performOtherRun();

        // System.out.println(FuzzInfo.generateClientReport());
		System.out.println(BeautifulReport.generateBeautifulReportWithFuzzInfo());
		
    }

	public boolean checkPause() {
		boolean pause = false;
		File pauseFile = new File(FileUtil.root + FileUtil.pause_file);
		if (pauseFile.exists()) {
			pause = true;
			// pauseFile.delete();
		}
		return pause;
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
					&& injected_fault.type == adjacentPoint.type
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
						&& injected_fault.type == adjacentPoint.type
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

	public void updateMetricOfQueuePair(SelectionInfo.QueuePair pair) {
		QueueEntry seed = pair.seed;
		QueueEntry mutate = pair.mutate;

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
