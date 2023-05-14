package edu.iscas.CCrashFuzzer.traditionalworkflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.Mutation;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;

// import edu.iscas.CCrashFuzzer.Mutation.EntryAndScore;

public class TranditionalFuzzingMutationSelector {

    static class EntryAndScore implements Comparable<EntryAndScore> {
    
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

    	public static void logScoresList(String title, List<EntryAndScore> list) {
    	    String logInfo = title + " score: {";
    	    for (int i = 0; i < list.size(); i++) {
    	        logInfo = logInfo + i + ":" + list.get(i).score + ",";
    	    }
    	    logInfo = logInfo + "}";
    	    Stat.log(EntryAndScore.class , logInfo);
    	}
    	
    }

    public static void appendGlobalNewIOSocre(List<EntryAndScore> list) {
    	for (EntryAndScore es: list) {
    		if (Mutation.checkIfEntryIsGlobalNewIO(es.entry)) {
    			es.score++;
    		}
    	}
    }

    public static void appendGlobalNewIOSocre(List<QueueEntry> mutates, List<Integer> scores) {
    	if (mutates.size() != scores.size()) return;
    	for (int i = 0; i < mutates.size(); i++) {
    		QueueEntry entry = mutates.get(i);
    		if (Mutation.checkIfEntryIsGlobalNewIO(entry)) {
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
    		if (Mutation.checkIfEntryIsGlobalNewIO(mutates.get(i))) {
    			scores[i]++;
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
    	result = TranditionalFuzzingMutationSelector.retriveAEntryListAndScoreBasedOnScore(mList, k);
    	return result;
    }

    public static List<QueueEntry> retriveAEntryListAndScoreBasedOnScore(List<EntryAndScore> list, int k) {
        List<QueueEntry> result = new ArrayList<>();
        if (list.size() == 0) {
            return result;
        }
        if (list.size() < k) {
    		for (EntryAndScore e: list) {
    			result.add(e.entry);
    		}
            return result;
        }
    	List<EntryAndScore> mList = new ArrayList<>(list);
    	for (int i = 0; i < k; i++) {
    		EntryAndScore e = TranditionalFuzzingMutationSelector.retriveAnEntryAndScoreBasedOnScore(mList);
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
            return list.get(EntryAndScore.rand.nextInt(list.size()));
        }
        int bound = EntryAndScore.rand.nextInt(totalSum);
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
    
}
