package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.Mutation.EntryAndScore;

public class SeedSelection {

    public static QueueEntry retrieveSeed(List<QueueEntry> candidate_queue) {
    	QueueEntry result = null;
    	if(candidate_queue == null ||candidate_queue.isEmpty()) {
    		return null;
    	}
    	List<EntryAndScore> list = new ArrayList<EntryAndScore>();
    	for (QueueEntry entry: candidate_queue) {
    		list.add(new EntryAndScore(entry, 0));
    	}

    	SeedSelection.appendGlobalNewIOSocre(list);
        SeedSelection.appendPerfSocre(list);

        logScoresList("seeds", list);

        list.sort(new Comparator<EntryAndScore>() {
            @Override
            public int compare(EntryAndScore o1, EntryAndScore o2) {
                return o2.score - o1.score;
            }
        });

    	result = list.get(0).entry;
    	return result;
    }

    public static void logScoresList(String title, List<EntryAndScore> list) {
        String logInfo = title + " score: {";
        for (int i = 0; i < list.size(); i++) {
            logInfo = logInfo + i + ":" + list.get(i).score + ",";
        }
        logInfo = logInfo + "}";
        Stat.log(logInfo);
    }

    public static void appendGlobalNewIOSocre(List<EntryAndScore> list) {
    	for(EntryAndScore e:list) {
    		QueueEntry q = e.entry;
    		// for (QueueEntry m:q.mutates) {
    		// 	if (Mutation.checkIfEntryIsGlobalNewIO(m)) {
    		// 		e.score += 1;
    		// 	}
    		// }
            List<FaultPoint> faultPointsToMutates = q.faultPointsToMutate;
            
            for (FaultPoint fp : faultPointsToMutates) {
                if (!QueueManagerNew.tested_fault_id.contains(fp.getFaultID())) {
                    e.score += 1;
                }
            }
    	}
    }

    public static void appendPerfSocre(List<EntryAndScore> list) {
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
            EntryAndScore e = list.get(i);
            if (scores[i] > midean) {
                e.score += 1;
            }
        }
        
        // int min = Integer.MAX_VALUE;
        // int max = Integer.MIN_VALUE;
        // for (EntryAndScore e : list) {
        //     QueueEntry q = e.entry;
        //     int score = q.getPerfScore();
        //     if (score < min) {
        //         min = score;
        //     }
        //     if (score > max) {
        //         max = score;
        //     }
        // }
        
         
         
        // for (EntryAndScore e : list) {
        //     QueueEntry q = e.entry;
        //     if (s)
        // }
    }

    
    
}
