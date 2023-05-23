package edu.iscas.tcse.faultfuzz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FaultSequence implements Iterable<FaultSequence.FaultPoint> {
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
								
	public int curAppear;

	@Override
    public Iterator<FaultPoint> iterator() {
        return seq.iterator();
    }

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
		// field tarNodeIp will be replcaed by field params since there are some faults that
		// need more than one parameter, such as networkConnect.
		@Deprecated
		public String tarNodeIp;

		/**
		 * Crashfuzz only use this field to determine which node to inject 
		 * in runtime. But not stored in fault sequence.
		 * In paricular, if in a test, targerNode is node 1 and actual node is node 2.
		 * After this test, the fault sequence is still thought to inject fault to node 1.
		 * And the actualNode will be reset to null in reset function. 
		 */
		public String actualNodeIp;  //fill at run time
		public int curAppear;

		public int curFWhAppear;

		public List<String> params;

		public FaultPoint() {
		}

		public FaultPoint(IOPoint ioPt, int ioPtIdx, FaultStat stat, FaultPos pos, String tarNodeIp,
				String actualNodeIp) {
			this.ioPt = ioPt;
			this.ioPtIdx = ioPtIdx;
			this.stat = stat;
			this.pos = pos;
			this.tarNodeIp = tarNodeIp;
			this.actualNodeIp = actualNodeIp;
		}

		public String toString() {
			return "FaultPoint=[ IOPoint=[" + ioPt.toString() + "]" + ", FaultStat=[" + stat + "], "
					+ ", FaultPos=[" + pos + "]"
					+ ", tarNodeIp=[" + tarNodeIp + "]"
					+ ", actualNodeIp=[" + actualNodeIp + "]"
					+ ", curFWhAppear=[" + curFWhAppear + "]"
					+ " ]";
		}
		
		public String getFaultInfo() {
			return ioPt.ioID+ioPt.appearIdx+pos.toString()+stat.toString();
		}

		public int getFaultID() {
			int result = (ioPt.CALLSTACK+stat.toString()+tarNodeIp).hashCode();
			return result;
		}

		public int getFaultIDWithOutIPInfo() {
			int result = (ioPt.CALLSTACK+stat.toString()).hashCode();
			return result;
		}

		// It semms the function never be used and CrashFuzz compute fault Id in another way
		// public int getFaultIDOld() {
		// 	return getFaultInfo().hashCode();
		// }
	}
	public enum FaultStat {
		NO, //we may not use this stat
		CRASH, 
		REBOOT,
		NETWORK_DISCONNECT,
		NETWORK_CONNECT
	}
	public enum FaultPos {
		BEFORE,AFTER
	}
	public String toString() {
		return seq.size()+" faults: "+seq.toString();
	}
}
