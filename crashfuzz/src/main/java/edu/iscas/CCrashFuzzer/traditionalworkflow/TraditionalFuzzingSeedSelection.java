package edu.iscas.CCrashFuzzer.traditionalworkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.Mutation;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.selection.OldQueueEntrySelector;
import edu.iscas.CCrashFuzzer.selection.OldQueueEntrySelector.QueuePair;

public class TraditionalFuzzingSeedSelection {

    static Random rand = new Random();

    @Deprecated
	public static List<QueuePair> retrievePairListInTranditionFuzzingProcess(List<QueueEntry> candidate_queue, Conf conf) {
		List<QueuePair> result = new ArrayList<QueuePair>();
		QueueEntry seed = TraditionalFuzzingSeedSelection.retrieveSeedInTranditionalFuzzingProcess(candidate_queue);
		int seedIdx = candidate_queue.indexOf(seed);
		Stat.log(OldQueueEntrySelector.class , "Select seed: " + seedIdx);
		if (!seed.was_fuzzed) {
			Mutation.mutateFaultSequence(seed, conf);
		}
		List<QueueEntry> mutations = TranditionalFuzzingMutationSelector.getMutationEntry(seed, conf);
		for (QueueEntry mutate: mutations) {
			QueuePair pair = new QueuePair();
			pair.seed = seed;
			pair.mutate = mutate;
			pair.seedIdx = seedIdx;
			pair.mutateIdx = seed.mutates.indexOf(mutate);
			Stat.log("Retrieve entry in retrievePairListFWH:"+pair.seedIdx+":"+pair.mutateIdx);
			result.add(pair);
		}
		return result;
	}

    public static QueueEntry retrieveSeedInTranditionalFuzzingProcess(List<QueueEntry> candidate_queue) {
    	QueueEntry result = null;
    	if(candidate_queue == null ||candidate_queue.isEmpty()) {
    		return null;
    	}
    	List<TranditionalFuzzingMutationSelector.EntryAndScore> list = new ArrayList<TranditionalFuzzingMutationSelector.EntryAndScore>();
    	for (QueueEntry entry: candidate_queue) {
    		list.add(new TranditionalFuzzingMutationSelector.EntryAndScore(entry, 0));
    	}

    	TraditionalFuzzingSeedSelection.appendGlobalNewIOSocre(list);
        TraditionalFuzzingSeedSelection.appendPerfSocre(list);

        TranditionalFuzzingMutationSelector.EntryAndScore.logScoresList("seeds", list);

        // list.sort(new Comparator<EntryAndScore>() {
        //     @Override
        //     public int compare(EntryAndScore o1, EntryAndScore o2) {
        //         return o2.score - o1.score;
        //     }
        // });

    	// result = list.get(0).entry;

        result = randomlyRetrieveASeedBasedOnScore(list);
    	return result;
    }

    public static QueueEntry randomlyRetrieveASeedBasedOnScore(List<TranditionalFuzzingMutationSelector.EntryAndScore> list) {
        QueueEntry result = null;
        TranditionalFuzzingMutationSelector.EntryAndScore e = TranditionalFuzzingMutationSelector.retriveAnEntryAndScoreBasedOnScore(list);
        if (e != null) {
            result = e.entry;
        }
        return result;
    }

    public static void appendGlobalNewIOSocre(List<TranditionalFuzzingMutationSelector.EntryAndScore> list) {
    	for(TranditionalFuzzingMutationSelector.EntryAndScore e:list) {
    		QueueEntry q = e.entry;
    		// for (QueueEntry m:q.mutates) {
    		// 	if (Mutation.checkIfEntryIsGlobalNewIO(m)) {
    		// 		e.score += 1;
    		// 	}
    		// }
            List<FaultPoint> faultPointsToMutates = q.faultPointsToMutate;
            
            for (FaultPoint fp : faultPointsToMutates) {
                if (!OldQueueEntrySelector.tested_fault_id.contains(fp.getFaultID())) {
                    e.score += 1;
                }
            }
    	}
    }

    public static void appendPerfSocre(List<TranditionalFuzzingMutationSelector.EntryAndScore> list) {
        /*
         * find the min and max perf score in entry.getPerfScore()
         */
        int[] scores = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            scores[i] = list.get(i).entry.getPerfScore();
        }
        int min = Arrays.stream(scores).min().getAsInt();
        int max = Arrays.stream(scores).max().getAsInt();
        int midean = (min + max) / 2;

        for (int i = 0; i < list.size(); i++) {
            TranditionalFuzzingMutationSelector.EntryAndScore e = list.get(i);
            if (scores[i] > midean) {
                // e.score += 1;
                e.score = e.score + e.score / 2;
            }
        }
    }

    
    
}
