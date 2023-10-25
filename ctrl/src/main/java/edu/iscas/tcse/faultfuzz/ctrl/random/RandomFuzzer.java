package edu.iscas.tcse.faultfuzz.ctrl.random;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.CoverageCollector;
import edu.iscas.tcse.faultfuzz.ctrl.FuzzInfo;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Monitor;
import edu.iscas.tcse.faultfuzz.ctrl.Network;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPos;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;
import edu.iscas.tcse.faultfuzz.ctrl.random.RandomFaultSequence.RandomFaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class RandomFuzzer {
	public static int MAP_SIZE = 100;
	private final RandomFuzzTarget target;
    private long totalSeedCases;
    private Conf conf;
    Monitor monitor;
    Stat stat;
    CoverageCollector coverage;
    public static boolean running = false;
    
    public static enum FaultCode{
      /* 00 */ FAULT_NONE,
      /* 01 */ FAULT_TMOUT,
      /* 02 */ FAULT_CRASH,
      /* 03 */ FAULT_ERROR,
      /* 04 */ FAULT_NOINST,
      /* 05 */ FAULT_NOBITS
    };
    
    public RandomFuzzer(RandomFuzzTarget target, Conf conf, boolean recover) {
    	monitor = new Monitor(conf);
    	stat = new Stat();
    	this.target = target;
    	this.conf = conf;
    	coverage = new CoverageCollector();
    	totalSeedCases = 0;
    }

    public static long getExecSeconds(long start) {
        return (((System.currentTimeMillis()-start)/ 1000));
    }

	//from 0 to limit-1
	public static int getRandomNumber(int limit) {
		int num = (int) (Math.random()*limit);
		return num;
	}
	
	public int common_fuzz_stuff(RandomQueueEntry q) {
		//save current test case to file
		//run_target
		//save_if_interesting
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
		HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
		faultsToTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
        save_if_interesting(q, rst, q.fname, "");
        
        if(rst == -1 || rst == 2) {//test again for not triggered cases and hang cases with a larger timeout
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
    		faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
    		faultsToTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    		faultsToTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
            save_if_interesting(q, rst, q.fname, "");
            
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
	public boolean save_if_interesting(RandomQueueEntry q, int faultMode, String testID, String seedName) {
		//check current rst:
		//save bugs
		//add instereting test cases to queue
//		monitor.generateAllFilesForTest(String.valueOf(totalExecutions), target.logInfo, q.faultSeq);
//		MyXMLReader.read_trace_map(monitor.getRootReport(String.valueOf(seq.hashCode()))+"cov/cov.xml");
//		FileUtil.copyFileToDir(conf.CUR_FAULT_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo(seedName,testID, target.logInfo);
		coverage.read_bitmap(FileUtil.root_tmp+testID+"/"+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
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
			if(nb>0) {//TODO: rethink this
//				Stat.markNewCoverage(tmpRootDir, nb);
				if(nb>0) {
					FuzzInfo.lastNewCovFaults = q.faultSeq.seq.size();
					HashMap<Integer, Integer> faultsToNewCovTests = FuzzInfo.timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
					faultsToNewCovTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
					faultsToNewCovTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
				}
				Stat.log("*********************Test "+testID+" causes new coverages*********************");
			}
		}
		q.bitmap_size = coverage.coveredBlocks(coverage.trace_bits);
		q.exec_s = target.a_exec_seconds;
		
		FuzzInfo.total_bitmap_size += q.bitmap_size;
		FuzzInfo.total_bitmap_entries++;
		FileUtil.writePostTestInfo(testID, q.bitmap_size, q.exec_s);
		FileUtil.copyToTested(testID, usedSeconds, conf);
//		FileUtil.delete(conf.CUR_FAULT_FILE.getAbsolutePath());
		if(!Conf.DEBUG) {
			FileUtil.delete(FileUtil.root_tmp+testID);
		}
		return true;
	}
	
	/* When we bump into a new path, we call this to see if the path appears
	   more "favorable" than any of the existing ones. The purpose of the
	   "favorables" is to have a minimal set of paths that trigger all the bits
	   seen in the bitmap so far, and focus on fuzzing them at the expense of
	   the rest.
	   The first step of the process is to maintain a list of top_rated[] entries
	   for every byte in the bitmap. We win that slot if there is no previous
	   contender, or if the contender has a more favorable speed x size factor. */

	public void update_bitmap_score(RandomQueueEntry q) {
	    
	}
	/* The second part of the mechanism discussed above is a routine that
	   goes over top_rated[] entries, and then sequentially grabs winners for
	   previously-unseen bytes (temp_v) and marks them as favored, at least
	   until the next run. The favored entries are given more air time during
	   all fuzzing steps. */
	public void cull_queue() {
	   
	}
	
	
	
	public int calculate_score(RandomQueueEntry q) {
		return 0;
		
	}
	public void perform_first_run() {
		//for the first run
		Stat.log("***********************Perform inital runs*****************************");
	    long start = System.currentTimeMillis();
		String testID = "init";
		target.run_target(RandomFaultSequence.getEmptyIns(), conf, "init", conf.hangSeconds);
		FuzzInfo.total_execs++;
		FuzzInfo.exec_us += target.a_exec_seconds;
		HashMap<Integer, Integer> faultsToTests = FuzzInfo.timeToFaulsToTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
		faultsToTests.computeIfAbsent(0, key -> 0);
		faultsToTests.computeIfPresent(0, (key, value) -> value + 1);
		String tmpRootDir = monitor.getTmpReportDir(testID);
		FileUtil.copyFileToDir(conf.CUR_FAULT_FILE.getAbsolutePath(), tmpRootDir);
		FileUtil.generateFAVLogInfo("", testID, target.logInfo, FaultSequence.createAnEmptyFaultSequence());
		
//		MyXMLReader.read_trace_map(monitor.getRootReport("init")+"cov/cov.xml");
		coverage.read_bitmap(tmpRootDir+FileUtil.coverageDir);
		int nb = coverage.has_new_bits();
		FileUtil.writeMap(testID, coverage.trace_bits, FuzzInfo.getTotalCoverage(coverage.trace_bits), nb);
		if(nb>0) {
			FuzzInfo.lastNewCovFaults = 0;
			HashMap<Integer, Integer> faultsToNewCovTests = FuzzInfo.timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (FuzzInfo.getUsedSeconds()/(FuzzInfo.reportWindow*60)), k -> new HashMap<Integer, Integer>());
			faultsToNewCovTests.computeIfAbsent(0, key -> 0);
			faultsToNewCovTests.computeIfPresent(0, (key, value) -> value + 1);
		}
		FileUtil.writePostTestInfo(testID, coverage.coveredBlocks(coverage.trace_bits), target.a_exec_seconds);
		totalSeedCases++;
		FuzzInfo.total_bitmap_size += coverage.coveredBlocks(coverage.trace_bits);
		FuzzInfo.total_bitmap_entries++;
		long usedSeconds = FuzzInfo.getUsedSeconds();
		FileUtil.copyToTested(testID, usedSeconds, conf);
		if(!Conf.DEBUG) {
			FileUtil.delete(tmpRootDir);
		}
        recordGlobalInfo();
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
        
        boolean hasFaultSequence = true;
        FuzzInfo.startTime = System.currentTimeMillis();
        try {
			FileUtils.forceMkdir(new File(FileUtil.root));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
        
        if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
        
        perform_first_run();
        
        while (( FuzzInfo.getUsedSeconds() < (conf.maxTestMinutes*60))) {
        	Stat.log("Generate a random fault sequence.");
        	RandomFaultSequence seq = generateRadomFaultSequence();
        	RandomQueueEntry q = new RandomQueueEntry();
        	q.faultSeq = seq;
        	common_fuzz_stuff(q);
        }
        
        System.out.println(FuzzInfo.generateClientReport());
    }
	
	public RandomFaultSequence generateRadomFaultSequence() {
		List<MaxDownNodes> currentCluster = MaxDownNodes.cloneCluster(conf.maxDownGroup);
		RandomFaultSequence randomFaults = new RandomFaultSequence();
		
		Random random = new Random();
		int faultNum = random.nextInt(conf.MAX_FAULTS)+1;
		if(Conf.DEBUG) {
			Stat.log("Fault number is "+faultNum);
		}
		
		List<String> servers = new ArrayList<>();
		for(MaxDownNodes sub:currentCluster) {
			if(Conf.DEBUG) {
				System.out.println(sub.maxDown);
				System.out.println(sub.aliveGroup);
				System.out.println(sub.deadGroup);
			}
			servers.addAll(sub.aliveGroup);
			servers.addAll(sub.deadGroup);
		}

		Network network = Network.constructNetworkFromMaxDOwnNodes(MaxDownNodes.cloneCluster(conf.maxDownGroup));
		
		if(Conf.MANUAL) {
        	Scanner scan = new Scanner(System.in);
        	scan.nextLine();
        }
		
		long avg_exec_us = (FuzzInfo.exec_us / FuzzInfo.total_execs);
		if(Conf.DEBUG) {
			Stat.log("average exec used seconds: "+avg_exec_us);
		}
		
		long timeOffSet = 0;
		
		for(int i = 0; i<faultNum; ) {
			int tarNode = random.nextInt(servers.size());
			String tarNodeIp = servers.get(tarNode);
			boolean canCrash = false;
			boolean canReboot = false;
			boolean faultInjected = false;
			
			Stat.debug("cur fault seq size " + randomFaults.seq.size());
			Stat.debug(i + "th Fault node is " + tarNodeIp);
			for(MaxDownNodes subCluster:currentCluster) {
				
				Stat.debug(subCluster.aliveGroup + " " + subCluster.aliveGroup.contains(tarNodeIp));
				Stat.debug(subCluster.deadGroup + " " + subCluster.deadGroup.contains(tarNodeIp));
				
				if(subCluster.aliveGroup.contains(tarNodeIp)
						|| subCluster.deadGroup.contains(tarNodeIp)) {
					canCrash = subCluster.aliveGroup.contains(tarNodeIp) && (subCluster.maxDown-1)>=0;
					canReboot = subCluster.deadGroup.size()>0 && subCluster.deadGroup.contains(tarNodeIp);

					boolean canDisconnectNetwork = Conf.faultTypeList.contains(FaultType.NETWORK_DISCONNECTION) ? checkCanDisconnectNetwork(network, subCluster, tarNodeIp) : false;
					boolean canConnectNetwork = Conf.faultTypeList.contains(FaultType.NETWORK_RECONNECTION) ? checkCanConnectNetwork(network, subCluster, tarNodeIp) : false;

					List<FaultType> faultTypeCouldInject = new ArrayList<>();

					Stat.debug(i + "th can crash " + canCrash + ", can reboot " + canReboot + ", can disnectNetwork "
							+ canDisconnectNetwork + ", can connectNetwork " + canConnectNetwork);
					Stat.debug(i + "th cur time offset is  " + timeOffSet);

					if (canCrash) {
						faultTypeCouldInject.add(FaultType.CRASH);
					}
					if (canReboot) {
						faultTypeCouldInject.add(FaultType.REBOOT);
					}
					if (canDisconnectNetwork) {
						faultTypeCouldInject.add(FaultType.NETWORK_DISCONNECTION);
					}
					if (canConnectNetwork) {
						faultTypeCouldInject.add(FaultType.NETWORK_RECONNECTION);
					}

					if (faultTypeCouldInject.size() == 0) {
						continue;
					} 

					FaultType stat = faultTypeCouldInject.get(random.nextInt(faultTypeCouldInject.size()));

					int waitTimeMillions = computeWaitTimeMillions(random, faultNum, avg_exec_us, timeOffSet, i);
					RandomFaultPoint p = null;
					switch (stat) {
						case CRASH:
							p = generateCrashPoint(tarNodeIp, waitTimeMillions);
							break;
						case REBOOT:
							p = generateRebootPoint(tarNodeIp, waitTimeMillions);
							break;
						case NETWORK_DISCONNECTION:
							String sourceIp = tarNodeIp;
							String destIp = randomChooseATargetNodeToDisconnect(network, subCluster, sourceIp);
							p = generateNetworkDisconnectPoint(sourceIp, destIp, waitTimeMillions);
							break;
						case NETWORK_RECONNECTION:
							String sourceIpInConnect = tarNodeIp;
							String destIpInConnect = randomChooseATargetNodeToConnect(network, subCluster, sourceIpInConnect);
							p = generateNetworkConnectPoint(sourceIpInConnect, destIpInConnect, waitTimeMillions);
							break;
						default:
							break;
					}

					timeOffSet = timeOffSet+waitTimeMillions;
					randomFaults.seq.add(p);

					switch (stat) {
						case CRASH:
							MaxDownNodes.buildClusterStatus(currentCluster, tarNodeIp, FaultType.CRASH);
							if (Conf.DEBUG) {
								Stat.log(i + "Crash node " + tarNodeIp + " after " + timeOffSet);
							}
							break;
						case REBOOT:
							MaxDownNodes.buildClusterStatus(currentCluster, tarNodeIp, FaultType.REBOOT);
							if (Conf.DEBUG) {
								Stat.log(i + "Reboot node " + tarNodeIp + " after " + timeOffSet);
							}
							break;
						case NETWORK_DISCONNECTION:
							String sourceIp = p.paras.get(0);
							String destIp = p.paras.get(1);
							network.disconnect(sourceIp, destIp);
							if (Conf.DEBUG) {
								Stat.log(i + " Network disconnection from " + sourceIp + " to " + tarNodeIp);
							}
							break;
						case NETWORK_RECONNECTION:
							String sourceIpInConnect = p.paras.get(0);
							String destIpInConnect = p.paras.get(1);
							network.connect(sourceIpInConnect, destIpInConnect);
							if (Conf.DEBUG) {
								Stat.log(i + " Network connection from " + sourceIpInConnect + " to " + destIpInConnect);
							}
							break;
						default:
							break;
					}

					faultInjected = true;
					break;
					
					
					// if(canCrash) {
					// 	int waitTimeMillions = computeWaitTimeMillions(random, faultNum, avg_exec_us, timeOffSet, i);

					// 	// RandomFaultPoint p  = new RandomFaultPoint();
					// 	// p.pos = FaultPos.BEFORE;
					// 	// p.tarNodeIp = tarNodeIp;
					// 	// p.stat = FaultStat.CRASH;
					// 	// p.actualNodeIp = null;
					// 	// p.waitTimeMillions = waitTimeMillions;
					// 	RandomFaultPoint p = generateCrashPoint(tarNodeIp, waitTimeMillions);
						
					// 	timeOffSet = timeOffSet+waitTimeMillions;
					// 	randomFaults.seq.add(p);
						
					// 	faultInjected = true;

					// 	if(Conf.DEBUG) {
					// 		Stat.log(i+"Crash node "+tarNodeIp+" after "+timeOffSet);
					// 	}
					// 	MaxDownNodes.buildClusterStatus(currentCluster, tarNodeIp, FaultStat.CRASH);
					// 	break;
					// } else if(canReboot) {

					// 	// int range = (int)(avg_exec_us*1000-timeOffSet-faultNum+i);
					// 	// range = range > 0? range: (int)(20000-timeOffSet-faultNum+i);
					// 	// if(Conf.DEBUG) {
					// 	// 	Stat.log(i+"th range is "+range);
					// 	// }

					// 	// RandomFaultPoint p  = new RandomFaultPoint();
					// 	// p.pos = FaultPos.BEFORE;
					// 	// p.tarNodeIp = tarNodeIp;
					// 	// p.stat = FaultStat.REBOOT;
					// 	// p.actualNodeIp = null;
					// 	// p.waitTimeMillions = random.nextInt(range);

					// 	int waitTimeMillions = computeWaitTimeMillions(random, faultNum, avg_exec_us, timeOffSet, i);
					// 	RandomFaultPoint p = generateRebootPoint(tarNodeIp, waitTimeMillions);

					// 	timeOffSet = timeOffSet+p.waitTimeMillions;
					// 	randomFaults.seq.add(p);
						
					// 	faultInjected = true;
						
					// 	if(Conf.DEBUG) {
					// 		Stat.log(i+"Reboot node "+tarNodeIp+" after "+timeOffSet);
					// 	}
						
					// 	MaxDownNodes.buildClusterStatus(currentCluster, tarNodeIp, FaultStat.REBOOT);
					// 	break;
					// } else if (canDisconnectNetwork) {

					// 	// int range = (int)(avg_exec_us*1000-timeOffSet-faultNum+i);
					// 	// range = range > 0? range: (int)(20000-timeOffSet-faultNum+i);
					// 	// if(Conf.DEBUG) {
					// 	// 	Stat.log(i+"th range is "+range);
					// 	// }

					// 	String sourceIp = tarNodeIp;
					// 	String destIp = randomChooseATargetNodeToDisconnect(network, subCluster, sourceIp);

					// 	// RandomFaultPoint p  = new RandomFaultPoint();
					// 	// p.pos = FaultPos.BEFORE;
					// 	// p.tarNodeIp = tarNodeIp;
					// 	// p.stat = FaultStat.NETWORK_DISCONNECT;
					// 	// p.actualNodeIp = null;

					// 	// p.paras = new ArrayList<>();
					// 	// p.paras.add(sourceIp);
					// 	// p.paras.add(destIp);
					// 	// p.waitTimeMillions = random.nextInt(range);

					// 	int waitTimeMillions = computeWaitTimeMillions(random, faultNum, avg_exec_us, timeOffSet, i);
					// 	RandomFaultPoint p = generateNetworkDisconnectPoint(sourceIp, destIp, waitTimeMillions);

					// 	timeOffSet = timeOffSet+p.waitTimeMillions;
					// 	randomFaults.seq.add(p);
						
					// 	faultInjected = true;

					// 	if(Conf.DEBUG) {
					// 		Stat.log(i+" Network disconnection from " + sourceIp+" to "+tarNodeIp);
					// 	}
					// 	network.disconnect(sourceIp, destIp);
					// } else if (canConnectNetwork) {

					// 	// int range = (int)(avg_exec_us*1000-timeOffSet-faultNum+i);
					// 	// range = range > 0? range: (int)(20000-timeOffSet-faultNum+i);
					// 	// if(Conf.DEBUG) {
					// 	// 	Stat.log(i+"th range is "+range);
					// 	// }

					// 	String sourceIp = tarNodeIp;
					// 	String destIp = randomChooseATargetNodeToConnect(network, subCluster, sourceIp);

					// 	// RandomFaultPoint p  = new RandomFaultPoint();
					// 	// p.pos = FaultPos.BEFORE;
					// 	// p.tarNodeIp = tarNodeIp;
					// 	// p.stat = FaultStat.NETWORK_RECONNECTION;
					// 	// p.actualNodeIp = null;
						
					// 	// p.paras = new ArrayList<>();
					// 	// p.paras.add(sourceIp);
					// 	// p.paras.add(destIp);
					// 	// p.waitTimeMillions = random.nextInt(range);

					// 	int waitTimeMillions = computeWaitTimeMillions(random, faultNum, avg_exec_us, timeOffSet, i);
					// 	RandomFaultPoint p = generateNetworkConnectPoint(sourceIp, destIp, waitTimeMillions);

					// 	timeOffSet = timeOffSet+p.waitTimeMillions;
					// 	randomFaults.seq.add(p);
						
					// 	faultInjected = true;

					// 	if(Conf.DEBUG) {
					// 		Stat.log(i+" Network connection from " + sourceIp+" to "+tarNodeIp);
					// 	}
					// 	network.connect(sourceIp, destIp);
					// }
				}
			}
			if(faultInjected) {
				i++;
			}
			if(Conf.MANUAL) {
	        	Scanner scan = new Scanner(System.in);
	        	scan.nextLine();
	        }
		}
		return randomFaults;
	}

	private int computeWaitTimeMillions(Random random, int faultNum, long avg_exec_us, long timeOffSet, int i) {
		int range = (int)(avg_exec_us*1000-timeOffSet-faultNum+i);
		range = range > 0? range: (int)(20000-timeOffSet-faultNum+i);
		if(Conf.DEBUG) {
			Stat.log(i+"th range is "+range);
		}
		int waitTimeMillions = random.nextInt(range);
		return waitTimeMillions;
	}

	public static boolean checkCanDisconnectNetwork(Network network, MaxDownNodes cluster) {
		if (cluster.aliveGroup.size() < 2) {
			return false;
		}
		for (String s: cluster.aliveGroup) {
			for (String s2: cluster.aliveGroup) {
				if (s.equals(s2)) {
					continue;
				}
				if (network.isConnected(s, s2)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean checkCanDisconnectNetwork(Network network, MaxDownNodes cluster, String sourceIp) {
		if (cluster.aliveGroup.size() < 2) {
			return false;
		}
		if (cluster.deadGroup.contains(sourceIp)) {
			return false;
		}
		for (String s: cluster.aliveGroup) {
			if (s.equals(sourceIp)) {
				continue;
			}
			if (network.isConnected(sourceIp, s)) {
				return true;
			}
		}
		return false;
	}

	public static String randomChooseATargetNodeToDisconnect(Network network, MaxDownNodes cluster, String sourceIp) {
		String result = null;
		Random r = new Random();
		List<String> candidateTarget = new ArrayList<>();
		for (String s: cluster.aliveGroup) {
			if (s.equals(sourceIp)) {
				continue;
			}
			if (network.isConnected(sourceIp, s)) {
				candidateTarget.add(s);
			}
		}
		if (candidateTarget.size() > 0) {
			int index = r.nextInt(candidateTarget.size());
			result = candidateTarget.get(index);
		}
		return result;
	}

	public static boolean checkCanConnectNetwork(Network network, MaxDownNodes cluster, String sourceIp) {
		if (cluster.aliveGroup.size() < 2) {
			return false;
		}
		if (cluster.deadGroup.contains(sourceIp)) {
			return false;
		}
		for (String s: cluster.aliveGroup) {
			if (s.equals(sourceIp)) {
				continue;
			}
			if (!network.isConnected(sourceIp, s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkCanConnectNetwork(Network network, MaxDownNodes cluster) {
		boolean result = false;
		for (String sourceIP: cluster.aliveGroup) {
			if (checkCanConnectNetwork(network, cluster, sourceIP)) {
				return true;
			}
		}
		return result;
	}

	public static String randomChooseATargetNodeToConnect(Network network, MaxDownNodes cluster, String sourceIp) {
		String result = null;
		Random r = new Random();
		List<String> candidateTarget = new ArrayList<>();
		for (String s: cluster.aliveGroup) {
			if (s.equals(sourceIp)) {
				continue;
			}
			if (!network.isConnected(sourceIp, s)) {
				candidateTarget.add(s);
			}
		}
		if (candidateTarget.size() > 0) {
			int index = r.nextInt(candidateTarget.size());
			result = candidateTarget.get(index);
		}
		return result;
	}

	public static RandomFaultPoint generateCrashPoint(String tarNodeIp, int waitTimeMillions) {
		RandomFaultPoint p = new RandomFaultPoint();
		p.pos = FaultPos.BEFORE;
		p.tarNodeIp = tarNodeIp;
		p.type = FaultType.CRASH;
		p.actualNodeIp = null;
		p.waitTimeMillions = waitTimeMillions;
		return p;
	}

	public static RandomFaultPoint generateRebootPoint(String tarNodeIp, int waitTimeMillions) {
		RandomFaultPoint p = new RandomFaultPoint();
		p.pos = FaultPos.BEFORE;
		p.tarNodeIp = tarNodeIp;
		p.type = FaultType.REBOOT;
		p.actualNodeIp = null;
		p.waitTimeMillions = waitTimeMillions;
		return p;
	}

	public static RandomFaultPoint generateNetworkDisconnectPoint(String sourceIp, String destIp,
			int waitTimeMillions) {
		RandomFaultPoint p = new RandomFaultPoint();
		p.pos = FaultPos.BEFORE;
		p.tarNodeIp = sourceIp;
		p.type = FaultType.NETWORK_DISCONNECTION;
		p.actualNodeIp = null;

		p.paras = new ArrayList<>();
		p.paras.add(sourceIp);
		p.paras.add(destIp);
		p.waitTimeMillions = waitTimeMillions;
		return p;
	}

	public static RandomFaultPoint generateNetworkConnectPoint(String sourceIp, String destIp, int waitTimeMillions) {
		RandomFaultPoint p = new RandomFaultPoint();
		p.pos = FaultPos.BEFORE;
		p.tarNodeIp = sourceIp;
		p.type = FaultType.NETWORK_RECONNECTION;
		p.actualNodeIp = null;

		p.paras = new ArrayList<>();
		p.paras.add(sourceIp);
		p.paras.add(destIp);
		p.waitTimeMillions = waitTimeMillions;
		return p;
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
