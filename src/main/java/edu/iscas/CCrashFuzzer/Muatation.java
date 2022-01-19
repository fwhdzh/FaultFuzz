package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;

public class Muatation {
	public static Set<FaultSequence> mutateFaultSequence(QueueEntry q) {
		Set<FaultSequence> mutates = new HashSet<FaultSequence>();
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
			mutates.add(seq);
			// Stat.log("Return mutates:"+mutates);
		}
		return mutates;
	}
	public static Set<FaultSequence> mutateTwoSimilarSeq(QueueEntry q){
		Set<FaultSequence> mutates = new HashSet<FaultSequence>();
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
				mutates.add(seq);
				
				FaultSequence seq2 = new FaultSequence();
				seq2.seq = new ArrayList<FaultPoint>();
				FaultPoint p2  = new FaultPoint();
				p2.ioPt = q.ioSeq.get(i+1);
				p2.pos = FaultPos.BEFORE;
				p2.tarNodeIp = p2.ioPt.ip;
				p2.stat = FaultStat.CRASH;
				seq2.seq.add(p2);
				mutates.add(seq2);
				break;
			}
			// Stat.log("Return mutates:"+mutates);
		}
		return mutates;
	}
	//from 0 to limit-1
		public static int getRandomNumber(int limit) {
			int num = (int) (Math.random()*limit);
			return num;
		}
}
