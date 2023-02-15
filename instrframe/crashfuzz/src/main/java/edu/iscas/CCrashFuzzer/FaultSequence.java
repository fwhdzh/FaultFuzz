package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FaultSequence {
	public static FaultSequence empty;
	static {
		empty = new FaultSequence();
		empty.curFault = new AtomicInteger(-1);
		empty.seq = new ArrayList<FaultPoint>();
	}
	public static FaultSequence getEmptyIns() {
		return empty;
	}
	public boolean isEmpty() {
		return this.equals(empty);
	}
	public void reset() {
		if(seq == null || seq.isEmpty()) {
			curFault.set(-1);;
		} else {
			curFault.set(0);
		}
		for(FaultPoint p:seq) {
			p.actualNodeIp = null;
			p.curAppear = 0;
		}
	}
	public FaultSequence() {
		curFault = new AtomicInteger(-1);
		seq = new ArrayList<FaultPoint>();
		on_recovery = false;
	}
	public List<FaultPoint> seq; //only contain the points that inject crash or reboot
	public AtomicInteger curFault;
    public boolean on_recovery;
	public int adjacent_new_covs; // for the last injected fault, the adjacent new covs of IO points in
	                              // similarBehaviorWindow
	public int getFaultSeqID() {
		String s = "";
		for(FaultPoint p:seq) {
			s += p.getFaultInfo();
		}
		return s.hashCode();
	}
	public static class FaultPoint {
		public IOPoint ioPt;
		public int ioPtIdx;
		public FaultStat stat;
		public FaultPos pos;//before or after
		public String tarNodeIp;
		public String actualNodeIp;  //fill at run time
		public int curAppear;
		public String toString() {
			return "FaultPoint=[ IOPoint=["+ioPt.toString()+"]"+", FaultStat=["+stat+"], "+", FaultPos=["+pos+"], "
		+"tarNodeIp=["+tarNodeIp+"], actualNodeIp=["+actualNodeIp+"] ]";
		}
		public int getFaultID() {
			return getFaultInfo().hashCode();
		}
		public String getFaultInfo() {
			return ioPt.ioID+ioPt.appearIdx+pos.toString()+stat.toString();
		}
	}
	public enum FaultStat {
		NO, //we may not use this stat
		CRASH, REBOOT
	}
	public enum FaultPos {
		BEFORE,AFTER
	}
	public String toString() {
		return seq.size()+" faults: "+seq.toString();
	}
}
