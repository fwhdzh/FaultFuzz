package edu.iscas.tcse.faultfuzz.ctrl.random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPos;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;

public class RandomFaultSequence {
	public static RandomFaultSequence empty;
	static {
		empty = new RandomFaultSequence();
		empty.curFault = new AtomicInteger(-1);
		empty.seq = new ArrayList<RandomFaultPoint>();
	}
	public static RandomFaultSequence getEmptyIns() {
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
		for(RandomFaultPoint p:seq) {
			p.actualNodeIp = null;
			p.curAppear = 0;
		}
	}
	public RandomFaultSequence() {
		curFault = new AtomicInteger(-1);
		seq = new ArrayList<RandomFaultPoint>();
	}
	public List<RandomFaultPoint> seq; //only contain the points that inject crash or reboot
	public AtomicInteger curFault;
	
	public static class RandomFaultPoint {
		public FaultType type;
		public FaultPos pos;//before or after
		public String tarNodeIp;
		public String actualNodeIp;  //fill at run time
		public int curAppear;
		public long waitTimeMillions;

		public List<String> paras;
		
		public String toString() {
			return "FaultPoint=[ WaitTime=["+waitTimeMillions+"]"+", FaultType=["+type+"], "+", FaultPos=["+pos+"], "
		+"tarNodeIp=["+tarNodeIp+"], actualNodeIp=["+actualNodeIp+"] ]";
		}
	}
	public String toString() {
		return seq.size()+" faults: "+seq.toString();
	}
}
