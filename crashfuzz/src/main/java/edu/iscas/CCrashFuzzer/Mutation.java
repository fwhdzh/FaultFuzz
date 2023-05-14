package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.Network.NetworkPath;
import edu.iscas.CCrashFuzzer.selection.OldQueueEntrySelector;

public class Mutation {

	static Random random = new Random();

	// public static void mutateFaultSequenceOld(QueueEntry q, Conf conf) {
	// 	List<QueueEntry> mutates = new ArrayList<QueueEntry>();
	// 	FaultSequence original_faults = q.faultSeq;
		
	// 	int io_index = q.candidate_io;
	// 	int fault_index = q.max_match_fault;
		
	// 	if(io_index == q.ioSeq.size() || fault_index < original_faults.seq.size() || original_faults.seq.size() >= conf.MAX_FAULTS) {
	// 		//no I/O points to inject a new fault
	// 		//or current I/O points do not match with the fault sequence
	// 		q.mutates = mutates;
	// 		// q.favored_mutates = q.mutates;
	// 		q.favored_mutates = new ArrayList<QueueEntry>(mutates);
	// 		return;
	// 	}


	// 	List<MaxDownNodes> currentCluster = MaxDownNodes.cloneCluster(conf.maxDownGroup);
	// 	for(FaultPoint fault:original_faults.seq) {
	// 		MaxDownNodes.buildClusterStatus(currentCluster, fault.tarNodeIp, fault.stat);
	// 	}
		
	// 	int lastIO = q.ioSeq.size();
	// 	Stat.log("Start to check fault point from "+io_index+" th I/O point for "+q.ioSeq.size()+" I/O points.");
		
	// 	for(int curIO = io_index; curIO< lastIO; curIO++) {
	// 		for(MaxDownNodes subCluster:currentCluster) {
	// 			if(subCluster.aliveGroup.contains(q.ioSeq.get(curIO).ip)
	// 					|| subCluster.deadGroup.contains(q.ioSeq.get(curIO).ip)) {
	// 				boolean canCrash = subCluster.aliveGroup.contains(q.ioSeq.get(curIO).ip) && (subCluster.maxDown-1)>=0;
	// 				boolean canReboot = subCluster.deadGroup.size()>0 && !subCluster.deadGroup.contains(q.ioSeq.get(curIO).ip);

	// 				boolean canDisconnectNetwork = subCluster.aliveGroup.contains(q.ioSeq.get(curIO).ip) && (q.ioSeq.get(curIO).PATH.startsWith("FAVMSG"))
	// 					&& (!q.ioSeq.get(curIO).PATH.startsWith("FAVMSG:READ"));

	// 				if(canCrash) {

	// 					IOPoint ioPt = q.ioSeq.get(curIO);
	// 					FaultPoint p = new FaultPoint(ioPt, curIO, FaultStat.CRASH, FaultPos.BEFORE, ioPt.ip, null);

	// 					FaultSequence faults = new FaultSequence();
	// 					prepareNewFaultSeqByAppendOneFaultToAnExsitingFaultSeq(original_faults, p, faults);

	// 					QueueEntry new_q = new QueueEntry(faults, q.ioSeq, true, q.bitmap_size, q.exec_s, 0);
						
	// 					updateUntestedFaultInformationField(q, p);
	// 					updateRecoveryInformationField(q, p, faults, new_q);
						
	// 					mutates.add(new_q);
	// 				}
	// 				if(canReboot) {
	// 					for(String rebootNode:subCluster.deadGroup) {
														
	// 						FaultPoint p = new FaultPoint(q.ioSeq.get(curIO), curIO, FaultStat.REBOOT, FaultPos.BEFORE, rebootNode, null);

	// 						FaultSequence faults = new FaultSequence();
	// 						prepareNewFaultSeqByAppendOneFaultToAnExsitingFaultSeq(original_faults, p, faults);
							
	// 						QueueEntry new_q = new QueueEntry(faults, q.ioSeq, true, q.bitmap_size, q.exec_s, 0);

	// 						updateUntestedFaultInformationField(q, p);
	// 						updateRecoveryInformationField(q, p, faults, new_q);
							
	// 						mutates.add(new_q);
	// 					}
	// 				}	
	// 				if (canDisconnectNetwork) {

	// 					IOPoint ioPt = q.ioSeq.get(curIO);
	// 					FaultPoint p = new FaultPoint(ioPt, curIO, FaultStat.NETWORK_DISCONNECT, FaultPos.BEFORE, ioPt.ip, null);

	// 					FaultSequence faults = new FaultSequence();
	// 					prepareNewFaultSeqByAppendOneFaultToAnExsitingFaultSeq(original_faults, p, faults);

	// 					QueueEntry new_q = new QueueEntry(faults, q.ioSeq, true, q.bitmap_size, q.exec_s, 0);

	// 					updateUntestedFaultInformationField(q, p);
	// 					updateRecoveryInformationField(q, p, faults, new_q);

	// 					mutates.add(new_q);
	// 				}
	// 			}
	// 		}
	// 	}
	// 	Stat.log("Got "+mutates.size()+" mutations.");

	// 	q.mutates = mutates;

	// 	// initializeLocalNotTestedFaultId(q);
	// 	// initializeOnRecoveryMutates(q);
	
	// 	q.favored_mutates = new ArrayList<QueueEntry>(mutates);
	// 	// q.globalNewIOMutates = collectGlobelNewIOMutates(q.favored_mutates);
	// }

	private static void prepareNewFaultSeqByAppendOneFaultToAnExsitingFaultSeq(FaultSequence original_faults, FaultPoint p, FaultSequence faults) {
		faults.seq = new ArrayList<FaultPoint>();
		faults.seq.addAll(original_faults.seq);
		faults.seq.add(p);
		faults.reset();
	}

	// private static void updateRecoveryInformationField(QueueEntry q, FaultPoint p, FaultSequence faults, QueueEntry new_q) {
	// 	if(q.on_recovery_mutates == null) {
	// 		q.on_recovery_mutates = new ArrayList<QueueEntry>();
	// 	}
	// 	if(q.recovery_io_id.contains(p.ioPt.ioID)) {
	// 		faults.on_recovery = true;
	// 	}
	// 	if(faults.on_recovery) {
	// 		q.on_recovery_mutates.add(new_q);
	// 	}
	// }

	// private static void updateUntestedFaultInformationField(QueueEntry q, FaultPoint p) {
	// 	if(q.not_tested_fault_id == null) {
	// 		q.not_tested_fault_id = new HashSet<Integer>();
	// 	}
	// 	int faultid = p.getFaultID();
	// 	q.not_tested_fault_id.add(faultid);
	// }

	public static void mutateFaultSequence(QueueEntry q, Conf conf) {
		
		List<QueueEntry> mutates = generateQueueEntry(q, conf);
		q.mutates = mutates;
		Stat.log("Got "+mutates.size()+" mutations.");
		q.favored_mutates = new ArrayList<QueueEntry>(mutates);
		initializeOnRecoveryMutates(q);
	}

	private static List<QueueEntry> generateQueueEntry(QueueEntry q, Conf conf) {
		List<QueueEntry> result = new ArrayList<QueueEntry>();
		List<FaultPoint> faultPointToMutate = q.faultPointsToMutate;
		FaultSequence original_faults = q.faultSeq;
		for (FaultPoint p : faultPointToMutate) {
			FaultSequence faults = new FaultSequence();
			prepareNewFaultSeqByAppendOneFaultToAnExsitingFaultSeq(original_faults, p, faults);

			QueueEntry new_q = new QueueEntry();
			new_q.ioSeq = q.ioSeq;
			new_q.faultSeq = faults;
			new_q.favored = true;
			new_q.exec_s = q.exec_s;
			new_q.bitmap_size = q.bitmap_size;
			new_q.handicap = 0;

			new_q.father = q;

			result.add(new_q);
		}

		return result;
	}

	private static List<FaultPoint> findFaultPointToInject(QueueEntry q, Conf conf) {

		List<FaultPoint> result = new ArrayList<>();

		List<QueueEntry> mutates = new ArrayList<QueueEntry>();
		FaultSequence original_faults = q.faultSeq;
		
		int io_index = q.candidate_io;
		int fault_index = q.max_match_fault;
		
		if(io_index == q.ioSeq.size() || fault_index < original_faults.seq.size() || original_faults.seq.size() >= conf.MAX_FAULTS) {
			//no I/O points to inject a new fault
			//or current I/O points do not match with the fault sequence
			q.mutates = mutates;
			// q.favored_mutates = q.mutates;
			q.favored_mutates = new ArrayList<QueueEntry>(mutates);
			return result;
		}


		List<MaxDownNodes> currentCluster = MaxDownNodes.cloneCluster(conf.maxDownGroup);
		for(FaultPoint fault:original_faults.seq) {
			MaxDownNodes.buildClusterStatus(currentCluster, fault.tarNodeIp, fault.stat);
		}

		Network network = Network.constructNetworkFromMaxDOwnNodes(MaxDownNodes.cloneCluster(conf.maxDownGroup));
		for(FaultPoint fault:original_faults.seq) {
			if (fault.stat == FaultStat.NETWORK_DISCONNECT) {
				List<String> msgInfo = fault.ioPt.getTotalInformationAboutMsgFromPath();
				String sourceIp = msgInfo.get(1);
				String destIp = msgInfo.get(2);
				network.disconnect(sourceIp, destIp);
			}
		}
		
		int lastIO = q.ioSeq.size();
		Stat.log("Start to check fault point from "+io_index+" th I/O point for "+q.ioSeq.size()+" I/O points.");
		
		for(int curIO = io_index; curIO< lastIO; curIO++) {
			for(MaxDownNodes subCluster:currentCluster) {
				IOPoint ioPointToInject = q.ioSeq.get(curIO);
				if(subCluster.aliveGroup.contains(ioPointToInject.ip)
						|| subCluster.deadGroup.contains(ioPointToInject.ip)) {
					
					boolean canCrash = Conf.s.contains(FaultStat.CRASH) ? (subCluster.aliveGroup.contains(ioPointToInject.ip) && (subCluster.maxDown-1)>=0) : false;
					boolean canReboot =  Conf.s.contains(FaultStat.REBOOT) ? (subCluster.deadGroup.size()>0 && !subCluster.deadGroup.contains(ioPointToInject.ip)) : false;
					boolean canDisconnectNetwork = Conf.s.contains(FaultStat.NETWORK_DISCONNECT) ? checkCanDisconnectNetwork(network, ioPointToInject, subCluster) : false;
					boolean canConnectNetwork = Conf.s.contains(FaultStat.NETWORK_CONNECT) ? checkCanConnectNetwork(network, ioPointToInject, subCluster) : false;
					if(canCrash) {
						FaultPoint p = new FaultPoint(ioPointToInject, curIO, FaultStat.CRASH, FaultPos.BEFORE, ioPointToInject.ip, null);
						result.add(p);
					}
					if(canReboot) {
						for(String rebootNode:subCluster.deadGroup) {
							FaultPoint p = new FaultPoint(ioPointToInject, curIO, FaultStat.REBOOT, FaultPos.BEFORE, rebootNode, null);
							result.add(p);
						}
					}
					if (canDisconnectNetwork) {
						FaultPoint p = new FaultPoint(ioPointToInject, curIO, FaultStat.NETWORK_DISCONNECT, FaultPos.BEFORE, ioPointToInject.ip, null);
						result.add(p);
					}
					if (canConnectNetwork) {
						for (NetworkPath path: network.disconnectedPath) {
							if (subCluster.deadGroup.contains(path.src)) {
								continue;
							}
							FaultPoint p = new FaultPoint(ioPointToInject, curIO, FaultStat.NETWORK_CONNECT, FaultPos.BEFORE, path.src, null);
							p.params = Arrays.asList(path.src, path.dest);
							result.add(p);
						}
					}
				}
			}
		}
		return result;
	}

	public static boolean checkCanDisconnectNetwork(Network network, IOPoint ioPt, MaxDownNodes cluster) {
		boolean result = false;
		// Injecting disconnection before a send msg is enough to
		// present all scenarios
		if (!ioPt.PATH.startsWith("FAVMSG:")) {
			result = false;
			return result;
		}
		List<String> msgInfo = ioPt.getTotalInformationAboutMsgFromPath();
		if (msgInfo.get(0).equals("READ")) {
			result = false;
			return result;
		}
		String sourceIp = msgInfo.get(1);
		String destIp = msgInfo.get(2);
		// Since we use iptables in source node, 
		// we only inject disconnection to alive nodes.
		if (cluster.deadGroup.contains(sourceIp)) {
			result = false;
			return result;
		}
		if (network.isConnected(sourceIp, destIp)) {
			result = true;
		}
		return result;
	}

	public static boolean checkCanConnectNetwork(Network network, IOPoint ioPt, MaxDownNodes cluster) {
		boolean result = false;
		for (Network.NetworkPath path: network.disconnectedPath) {
			String sourceIp = path.src;
			if (!cluster.deadGroup.contains(sourceIp)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static void initializeFaultPointsToMutate(QueueEntry q, Conf conf) {
		List<FaultPoint> faults = findFaultPointToInject(q, conf);
		q.faultPointsToMutate = faults;
	}

	public static void initializeLocalNotTestedFaultId(QueueEntry seedQ) {
		Set<Integer> r = new HashSet<>();
		for (FaultPoint p: seedQ.faultPointsToMutate) {
			int faultId = p.getFaultID();
			r.add(faultId);
		}
		seedQ.not_tested_fault_id = r;
	}

	private static void initializeOnRecoveryMutates(QueueEntry seedQ) {
		List<QueueEntry> r = new ArrayList<>();
		for (QueueEntry mutate: seedQ.mutates) {
			FaultPoint p = mutate.faultSeq.seq.get(mutate.faultSeq.seq.size()-1);
			if (seedQ.recovery_io_id.contains(p.ioPt.ioID)) {
				r.add(mutate);
			}
		}
		seedQ.on_recovery_mutates = r;
	}

	// public static List<QueueEntry> collectGlobelNewIOMutates(List<QueueEntry> entryList) {
	// 	List<QueueEntry> result = new ArrayList<QueueEntry>();
	// 	for (QueueEntry entry: entryList) {
	// 		boolean haveGloablNewIO = false;
	// 		List<FaultPoint> seq = entry.faultSeq.seq;
	// 		for (FaultPoint fp: seq) {
	// 			if (QueueManagerNew.tested_fault_id.contains(fp.getFaultID())) {
	// 				haveGloablNewIO = true;
	// 				break;
	// 			}
	// 		}
	// 		if (haveGloablNewIO) {
	// 			result.add(entry);
	// 		}
	// 	}
	// 	return result;
	// }

	// public static List<QueueEntry> getToTestEneryAndRemoveThemFromRespondingQueue(QueueEntry entry) {
	// 	List<QueueEntry> result = new ArrayList<QueueEntry>();
		
	// 	return result;
	// }

	public static class EntryAndScore implements Comparable<EntryAndScore> {

		static Random rand = new Random();

		public QueueEntry entry;
		public int score;
		public EntryAndScore(QueueEntry entry, int score) {
			this.entry = entry;
			this.score = score;
		}
		@Override
		public int compareTo(EntryAndScore o) {
			// TODO Auto-generated method stub
			int result = this.score - o.score;
			// result = 0 - result;
			return result;
		}

		public static List<QueueEntry> retriveAEntryListAndScoreBasedOnScore(List<Mutation.EntryAndScore> list, int k) {
		    List<QueueEntry> result = new ArrayList<>();
		    if (list.size() == 0) {
		        return result;
		    }
		    if (list.size() < k) {
				for (Mutation.EntryAndScore e: list) {
					result.add(e.entry);
				}
		        return result;
		    }
			List<Mutation.EntryAndScore> mList = new ArrayList<>(list);
			for (int i = 0; i < k; i++) {
				Mutation.EntryAndScore e = Mutation.EntryAndScore.retriveAnEntryAndScoreBasedOnScore(mList);
				result.add(e.entry);
				mList.remove(e);
			}
		    return result;
		}

		public static EntryAndScore retriveAnEntryAndScoreBasedOnScore(List<EntryAndScore> list) {
            EntryAndScore result = null;
            if (list.size() == 0) {
                return null;
            }
            int totalSum = 0;
            for (EntryAndScore e:list) {
                totalSum = totalSum + e.score;
            }
            if (totalSum == 0) {
                return list.get(rand.nextInt(list.size()));
            }
            int bound = rand.nextInt(totalSum);
			Stat.log(EntryAndScore.class, "bound selected is: " + bound);
            int nTotal = 0;
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                nTotal = nTotal + list.get(i).score;
                if (nTotal > bound) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                result = list.get(index);
            }
			Stat.log(EntryAndScore.class, "selected index is: " + index);
            return result;
        }

        public static void logScoresList(String title, List<EntryAndScore> list) {
		    String logInfo = title + " score: {";
		    for (int i = 0; i < list.size(); i++) {
		        logInfo = logInfo + i + ":" + list.get(i).score + ",";
		    }
		    logInfo = logInfo + "}";
		    Stat.log(EntryAndScore.class , logInfo);
		}
		
	}

	public static boolean checkIfEntryIsGlobalNewIO(QueueEntry entry) {
		boolean result = false;
		FaultPoint lastFault = entry.faultSeq.seq.get(entry.faultSeq.seq.size() - 1);
		int id = lastFault.getFaultID();
		result = !OldQueueEntrySelector.tested_fault_id.contains(id);
		return result;
	}

	public static void appendGlobalNewIOSocre(List<QueueEntry> mutates, List<Integer> scores) {
		if (mutates.size() != scores.size()) return;
		for (int i = 0; i < mutates.size(); i++) {
			QueueEntry entry = mutates.get(i);
			if (checkIfEntryIsGlobalNewIO(entry)) {
				int s = scores.get(i);
				s = s + 1;
				scores.set(i, s);
			}
		}
	}

	public static void appendGlobalNewIOSocre(List<QueueEntry> mutates, int[] scores) {
		if (mutates.size() != scores.length)
			return;
		for (int i = 0; i < mutates.size(); i++) {
			if (checkIfEntryIsGlobalNewIO(mutates.get(i))) {
				scores[i]++;
			}
		}
	}

	public static void appendGlobalNewIOSocre(List<EntryAndScore> list) {
		for (EntryAndScore es: list) {
			if (checkIfEntryIsGlobalNewIO(es.entry)) {
				es.score++;
			}
		}
	}

	public static boolean checkIfEntryIsRecovery(QueueEntry entry) {
		boolean result = false;
		result = entry.faultSeq.on_recovery;
		return result;
	}

	public static void appendRecoveryScore(List<EntryAndScore> list) {
		for (EntryAndScore es: list) {
			if (checkIfEntryIsRecovery(es.entry)) {
				es.score++;
			}
		}
	}

	public static void appendLocalNewIOScore(QueueEntry oriEntry, List<EntryAndScore> list) {
		for (EntryAndScore es : list) {
			List<FaultPoint> seq = es.entry.faultSeq.seq;
			FaultPoint lastFault = seq.get(seq.size() - 1);
			int id = lastFault.getFaultID();
			if (oriEntry.not_tested_fault_id.contains(id)) {
				es.score++;
			}
		}
	}


	
	public static List<QueueEntry> mutateFaultSequence_backup(QueueEntry q) {
		List<QueueEntry> mutates = new ArrayList<QueueEntry>();
//		int random = getRandomNumber(q.ioSeq.size());
		for(IOPoint pickedPt:q.ioSeq) {
			FaultSequence seq = new FaultSequence();
			seq.seq = new ArrayList<FaultPoint>();
			FaultPoint p  = new FaultPoint();
			p.ioPt = pickedPt;
			p.pos = FaultPos.BEFORE;
			p.tarNodeIp = p.ioPt.ip;
			p.stat = FaultStat.CRASH;
			seq.seq.add(p);
//			mutates.add(seq);
			QueueEntry new_q = new QueueEntry();
			new_q.ioSeq = q.ioSeq;
			new_q.faultSeq = seq;
			new_q.favored = true;
			mutates.add(new_q);
			// Stat.log("Return mutates:"+mutates);
		}
		return mutates;
	}
	public static List<QueueEntry> mutateTwoSimilarSeq(QueueEntry q){
		List<QueueEntry> mutates = new ArrayList<QueueEntry>();
//		int random = getRandomNumber(q.ioSeq.size());
		for(int i = 0; i<q.ioSeq.size()-1; i++) {
			if(q.ioSeq.get(i).CALLSTACK.toString().equals(q.ioSeq.get(i+1).CALLSTACK.toString())) {
				FaultSequence seq = new FaultSequence();
				seq.seq = new ArrayList<FaultPoint>();
				FaultPoint p  = new FaultPoint();
				p.ioPt = q.ioSeq.get(i);
				p.pos = FaultPos.BEFORE;
				p.tarNodeIp = p.ioPt.ip;
				p.stat = FaultStat.CRASH;
				seq.seq.add(p);

				QueueEntry new_q1 = new QueueEntry();
				new_q1.ioSeq = q.ioSeq;
				new_q1.faultSeq = seq;
				new_q1.favored = true;
				mutates.add(new_q1);
				
				FaultSequence seq2 = new FaultSequence();
				seq2.seq = new ArrayList<FaultPoint>();
				FaultPoint p2  = new FaultPoint();
				p2.ioPt = q.ioSeq.get(i+1);
				p2.pos = FaultPos.BEFORE;
				p2.tarNodeIp = p2.ioPt.ip;
				p2.stat = FaultStat.CRASH;
				seq2.seq.add(p2);
				
				QueueEntry new_q2 = new QueueEntry();
				new_q2.ioSeq = q.ioSeq;
				new_q2.faultSeq = seq2;
				new_q2.favored = true;
				mutates.add(new_q2);
				break;
			}
		}

		 Stat.log("Return mutates:"+mutates.size());
		return mutates;
	}
	
	// from 0 to limit-1
	public static int getRandomNumber(int limit) {
		int num = (int) (Math.random() * limit);
		return num;
	}

	public static List<QueueEntry> getMutationEntry(QueueEntry seed, Conf conf) {
		List<QueueEntry> result = new ArrayList<QueueEntry>();
		List<EntryAndScore> mList = new ArrayList<EntryAndScore>();
		for (QueueEntry entry : seed.mutates) {
			mList.add(new EntryAndScore(entry, 0));
		}

		appendGlobalNewIOSocre(mList);
		appendRecoveryScore(mList);
		appendLocalNewIOScore(seed, mList);

		EntryAndScore.logScoresList("mutates", mList);

		// mList.sort(new Comparator<EntryAndScore>() {
		// 	@Override
		// 	public int compare(EntryAndScore o1, EntryAndScore o2) {
		// 		return o2.score - o1.score;
		// 	}
		// });

		// int k = conf.MUTATE_CHOOSE;
		// for (int i = 0; i < k; i++) {
		// 	result.add(mList.get(i).entry);
		// }

		int k = conf.FAULT_SEQUENCE_BATCH_SIZE;
		result = EntryAndScore.retriveAEntryListAndScoreBasedOnScore(mList, k);
		return result;
	}
}
