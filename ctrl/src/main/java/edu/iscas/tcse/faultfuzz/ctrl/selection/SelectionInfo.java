package edu.iscas.tcse.faultfuzz.ctrl.selection;

import java.util.HashSet;
import java.util.Set;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;

public class SelectionInfo {

    public static class QueuePair {
    	public QueueEntry seed;
    	public QueueEntry mutate;
    
    	/*
    	 * Don't use this to remove QueueEntry from queue!
    	 * If we test more than one QueueEntry as a batch, the index could be changed！
    	 * Use indexOf(seed) or indexOf(mutate) instead!
    	 */
    	public int seedIdx;
    	public int mutateIdx;
    }

    public static Set<Integer> tested_fault_id = new HashSet<Integer>();
    public static Set<FaultPoint> testedFault = new HashSet<FaultPoint>();
	
	public static boolean checkIfEntryIsGlobalNewIO(QueueEntry entry) {
		boolean result = false;
		FaultPoint lastFault = entry.faultSeq.seq.get(entry.faultSeq.seq.size() - 1);
		int id = lastFault.getFaultID();
		result = !tested_fault_id.contains(id);
		return result;
	}
}
