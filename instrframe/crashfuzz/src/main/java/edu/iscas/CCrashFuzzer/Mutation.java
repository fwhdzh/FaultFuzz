package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;

public class Mutation {
	public static void buildClusterStatus(List<MaxDownNodes> currentCluster, String faultNodeIp, FaultStat faultType) {
		for(MaxDownNodes subCluster:currentCluster) {
//			System.out.println("mutation, maxDown "+subCluster.maxDown
//					+", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
			if(subCluster.aliveGroup.contains(faultNodeIp) && faultType.equals(FaultStat.CRASH)) {
				subCluster.maxDown--;
				subCluster.aliveGroup.remove(faultNodeIp);
				subCluster.deadGroup.add(faultNodeIp);

//				System.out.println("mutation, move "+faultNodeIp+" from alive to dead."+subCluster.maxDown);
				break;
			} else if(subCluster.deadGroup.contains(faultNodeIp) && faultType.equals(FaultStat.REBOOT)) {
				subCluster.maxDown++;
				subCluster.deadGroup.remove(faultNodeIp);
				subCluster.aliveGroup.add(faultNodeIp);
//				System.out.println("mutation, move "+faultNodeIp+" from dead to alive."+subCluster.maxDown);
				break;
			} else {
				continue;
			}
		}
	}
	public static boolean isAliveNode(List<MaxDownNodes> currentCluster, String faultNodeIp) {
		for(MaxDownNodes subCluster:currentCluster) {
//			System.out.println("mutation, maxDown "+subCluster.maxDown
//					+", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
			if(subCluster.aliveGroup.contains(faultNodeIp)) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}
	public static boolean isDeadNode(List<MaxDownNodes> currentCluster, String faultNodeIp) {
		for(MaxDownNodes subCluster:currentCluster) {
//			System.out.println("mutation, maxDown "+subCluster.maxDown
//					+", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
			if(subCluster.deadGroup.contains(faultNodeIp)) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}
	public static List<MaxDownNodes> cloneCluster(List<MaxDownNodes> srcCluster) {
		List<MaxDownNodes> desCluster = new ArrayList<MaxDownNodes>();
		for(MaxDownNodes sub:srcCluster) {
    		MaxDownNodes group = new MaxDownNodes();
    		group.maxDown = sub.maxDown;
    		group.aliveGroup = new HashSet<String>();
    		group.aliveGroup.addAll(sub.aliveGroup);
    		
    		group.deadGroup = new HashSet<String>();
    		group.deadGroup.addAll(sub.deadGroup);
    		desCluster.add(group);
    	}
		return desCluster;
	}
	public static void mutateFaultSequence(QueueEntry q, Conf conf) {
		List<QueueEntry> mutates = new ArrayList<QueueEntry>();
		FaultSequence original_faults = q.faultSeq;
		
		int io_index = q.candidate_io;
		int fault_index = q.max_match_fault;
		
		if(io_index == q.ioSeq.size() || fault_index < original_faults.seq.size() || original_faults.seq.size() >= conf.MAX_FAULTS) {
			//no I/O points to inject a new fault
			//or current I/O points do not match with the fault sequence
			q.mutates = mutates;
			q.favored_mutates = q.mutates;
			return;
		}


		List<MaxDownNodes> currentCluster = Mutation.cloneCluster(conf.maxDownGroup);
		for(FaultPoint fault:original_faults.seq) {
			buildClusterStatus(currentCluster, fault.tarNodeIp, fault.stat);
		}
		
		int lastIO = q.ioSeq.size();
		Stat.log("Start to check fault point from "+io_index+" th I/O point for "+q.ioSeq.size()+" I/O points.");
		
		for(int curIO = io_index; curIO< lastIO; curIO++) {
			/*
			List<IOPoint> beforeNeighbors = new ArrayList<IOPoint>();
			List<IOPoint> afterNeighbors = new ArrayList<IOPoint>();
			int adjacentNewCovs = 0;
			for(int i = io_index-1; i >=0; i--) {
				if((q.ioSeq.get(io_index).TIMESTAMP - q.ioSeq.get(i).TIMESTAMP) <= conf.similarBehaviorWindow) {
					beforeNeighbors.add(q.ioSeq.get(i));
					adjacentNewCovs += q.ioSeq.get(i).newCovs;
				} else {
					break;
				}
			}
			for(int i = io_index; i < lastIO; i++) {
				if((q.ioSeq.get(i).TIMESTAMP - q.ioSeq.get(io_index).TIMESTAMP) <= conf.similarBehaviorWindow) {
					afterNeighbors.add(q.ioSeq.get(i));
					adjacentNewCovs += q.ioSeq.get(i).newCovs;
				} else {
					break;
				}
			}
			*/
			for(MaxDownNodes subCluster:currentCluster) {
				if(subCluster.aliveGroup.contains(q.ioSeq.get(curIO).ip)
						|| subCluster.deadGroup.contains(q.ioSeq.get(curIO).ip)) {
					boolean canCrash = subCluster.aliveGroup.contains(q.ioSeq.get(curIO).ip) && (subCluster.maxDown-1)>=0;
					boolean canReboot = subCluster.deadGroup.size()>0 && !subCluster.deadGroup.contains(q.ioSeq.get(curIO).ip);
					if(canCrash) {
						FaultSequence faults = new FaultSequence();
						faults.seq = new ArrayList<FaultPoint>();
						faults.seq.addAll(original_faults.seq);
						FaultPoint p  = new FaultPoint();
						p.ioPt = q.ioSeq.get(curIO);
						p.ioPtIdx = curIO;
						p.pos = FaultPos.BEFORE;
						p.tarNodeIp = p.ioPt.ip;
						p.stat = FaultStat.CRASH;
						p.actualNodeIp = null;
						faults.seq.add(p);
						if(q.recovery_io_id.contains(p.ioPt.ioID)) {
							faults.on_recovery = true;
						}
						faults.reset();
//						faults.adjacent_new_covs = adjacentNewCovs;
						
						QueueEntry new_q = new QueueEntry();
						new_q.ioSeq = q.ioSeq;
						new_q.faultSeq = faults;
						new_q.favored = true;
						new_q.exec_s  = q.exec_s;
						new_q.bitmap_size = q.bitmap_size;
						new_q.handicap = 0;
						
						if(q.not_tested_fault_id == null) {
							q.not_tested_fault_id = new HashSet<Integer>();
						}
						if(q.on_recovery_mutates == null) {
							q.on_recovery_mutates = new ArrayList<QueueEntry>();
						}
						int faultid = (p.ioPt.CALLSTACK+p.stat.toString()+p.tarNodeIp).hashCode();
						q.not_tested_fault_id.add(faultid);
						if(faults.on_recovery) {
							q.on_recovery_mutates.add(new_q);
						}
						
						mutates.add(new_q);
					}
					if(canReboot) {
						for(String rebootNode:subCluster.deadGroup) {
							FaultSequence faults = new FaultSequence();
							faults.seq = new ArrayList<FaultPoint>();
							faults.seq.addAll(original_faults.seq);
							FaultPoint p  = new FaultPoint();
							p.ioPt = q.ioSeq.get(curIO);
							p.ioPtIdx = curIO;
							p.pos = FaultPos.BEFORE;
							p.tarNodeIp = rebootNode;
							p.stat = FaultStat.REBOOT;
							p.actualNodeIp = null;
							faults.seq.add(p);
							if(q.recovery_io_id.contains(p.ioPt.ioID)) {
								faults.on_recovery = true;
							}
							faults.reset();
//							faults.adjacent_new_covs = adjacentNewCovs;
							
							QueueEntry new_q = new QueueEntry();
							new_q.ioSeq = q.ioSeq;
							new_q.faultSeq = faults;
							new_q.favored = true;
							new_q.exec_s  = q.exec_s;
							new_q.bitmap_size = q.bitmap_size;
							new_q.handicap = 0;

							if(q.not_tested_fault_id == null) {
								q.not_tested_fault_id = new HashSet<Integer>();
							}
							if(q.on_recovery_mutates == null) {
								q.on_recovery_mutates = new ArrayList<QueueEntry>();
							}
							int faultid = (p.ioPt.CALLSTACK+p.stat.toString()+p.tarNodeIp).hashCode();
							q.not_tested_fault_id.add(faultid);
							if(faults.on_recovery) {
								q.on_recovery_mutates.add(new_q);
							}
							
							mutates.add(new_q);
						}
					}
				}
			}
		}
		Stat.log("Got "+mutates.size()+" mutations.");

		q.mutates = mutates;
		q.favored_mutates = q.mutates;
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
	//from 0 to limit-1
		public static int getRandomNumber(int limit) {
			int num = (int) (Math.random()*limit);
			return num;
		}
}
