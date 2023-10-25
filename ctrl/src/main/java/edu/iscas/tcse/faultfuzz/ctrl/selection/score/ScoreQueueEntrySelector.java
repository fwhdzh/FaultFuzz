package edu.iscas.tcse.faultfuzz.ctrl.selection.score;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;

public class ScoreQueueEntrySelector {

    // public static class ScoreField {
    //     public String desc;
    //     public int score;
    // } 

    public static EntryAndScore retriveAnEntryAndScoreBasedOnScore(List<EntryAndScore> list) {
        EntryAndScore result = null;
        if (list.size() == 0) {
            return null;
        }
        long totalSum = 0;
        for (EntryAndScore e:list) {
            totalSum = totalSum + e.score;
        }
        if (totalSum == 0) {
            return list.get(EntryAndScore.rand.nextInt(list.size()));
        }
        // long bound = EntryAndScore.rand.nextLong(totalSum);
        long bound = EntryAndScore.rand.nextLong() % totalSum;
        /*
         * Be careful that rand.nextLong could return a negative number!
         */
        bound = Math.abs(bound);
    	Stat.log(EntryAndScore.class, "bound selected is: " + bound);
        long nTotal = 0;
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
    	Stat.log(EntryAndScore.class, "Going to test the " + index + "th fault sequence! the score of it is: " + result.score);
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
    		EntryAndScore e = retriveAnEntryAndScoreBasedOnScore(mList);
    		result.add(e.entry);
    		mList.remove(e);
    	}
        return result;
    }

    public static List<SelectionInfo.QueuePair> retrieveAPairList(List<QueueEntry> candidate_queue, Conf conf) {
        List<SelectionInfo.QueuePair> result = new ArrayList<SelectionInfo.QueuePair>();
        List<QueueEntry> queue = new ArrayList<QueueEntry>();
        for (QueueEntry seed: candidate_queue) {
            for (QueueEntry m:seed.mutates) {
                queue.add(m);
            }
        }

        // List<EntryAndScore> list = ScoreAddedStrategy.computeTotalScore(queue);
        // List<EntryAndScore> list = ScoreCombinedStrategy.computeTotalScore(queue);
        // List<EntryAndScore> list = FWHScoreStrategy.computeTotalScore(queue);
        // List<EntryAndScore> list = FaultFuzzScoreStrategy.computeTotalScore(queue);
        List<EntryAndScore> list = WorkloadSocreStrategy.computeTotalScore(queue);

        EntryAndScore.logScoresList("retrieveAPairList", list);
        int k = conf.FAULT_SEQUENCE_BATCH_SIZE;
        List<QueueEntry> entryList = retriveAEntryListAndScoreBasedOnScore(list, k);
        for (QueueEntry m: entryList) {
            SelectionInfo.QueuePair pair = new SelectionInfo.QueuePair();
            pair.seed = m.seed;
            pair.seedIdx = candidate_queue.indexOf(pair.seed);
            pair.mutate = m;
            pair.mutateIdx = pair.seed.mutates.indexOf(pair.mutate);
            result.add(pair);
        }
        return result;
    }
}
