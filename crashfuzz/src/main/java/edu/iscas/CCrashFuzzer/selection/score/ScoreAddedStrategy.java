package edu.iscas.CCrashFuzzer.selection.score;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.selection.OldQueueEntrySelector;

@Deprecated
public class ScoreAddedStrategy {

    public static void appendBasicSocre(List<EntryAndScore> list) {
        for(EntryAndScore e:list) {
            e.score = 100;
        }
    }

    public static void appendPerfSocre(List<EntryAndScore> list) {
        for(EntryAndScore e:list) {
            int perfScore = e.entry.father.getPerfScore();
            e.score += perfScore;
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
                es.score = es.score * 2;
            }
        }
    }

    public static void decreaseNodeSymmetrySocre(List<EntryAndScore> list) {
        for (EntryAndScore es: list) {
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
            if (OldQueueEntrySelector.tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                es.score /= 2;
            }
        }
    }

    public static void decreaseCotinuesIPPointInOneNodeSocre(List<EntryAndScore> list) {
        for (EntryAndScore es: list) {
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
            if (es.entry.father.not_tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                es.score /= 2;
            }
        }
    }

    public static List<EntryAndScore> computeTotalScore(List<QueueEntry> queue) {
        List<EntryAndScore> result = new ArrayList<EntryAndScore>();
        for (QueueEntry qe: queue) {
            EntryAndScore es = new EntryAndScore(qe, 0);
            result.add(es);
        }
        appendBasicSocre(result);
        appendPerfSocre(result);
        appendRecoveryScore(result);
        decreaseNodeSymmetrySocre(result);
        decreaseCotinuesIPPointInOneNodeSocre(result);
        return result; 
    }
}