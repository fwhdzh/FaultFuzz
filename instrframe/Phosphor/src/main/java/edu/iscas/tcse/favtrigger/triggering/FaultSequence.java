package edu.iscas.tcse.favtrigger.triggering;

import java.util.ArrayList;
import java.util.List;

public class FaultSequence {
	public static FaultSequence empty;
	static {
		empty = new FaultSequence();
		empty.curFault = -1;
		empty.seq = new ArrayList<FaultPoint>();
	}
	public static FaultSequence getEmptyIns() {
		return empty;
	}
	public boolean isEmpty() {
		return this.equals(empty);
	}
	List<FaultPoint> seq; //only contain the points that inject crash or reboot
	int curFault;
	int curAppear;
	public static class FaultPoint {
		IOPoint ioPt;
		FaultStat stat;
		FaultPos pos;
		String tarNodeIp;
		String actualNodeIp;  //fill at run time
		public String toString() {
			return "FaultPoint=[ IOPoint=["+ioPt.toString()+"]"+", FaultStat=["+stat+"], "+", FaultPos=["+pos+"], "
		+"tarNodeIp=["+tarNodeIp+"], actualNodeIp=["+actualNodeIp+"] ]";
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
		return seq.toString();
	}
}
