package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.QueueManagerNew.QueuePair;

public class QueueManagerBruteForce {
    public static QueuePair retrieveAnEntry(List<QueueEntry> candidate_queue) {
        QueuePair result = new QueuePair();;
        if (candidate_queue == null || candidate_queue.size() == 0) {
            return null;
        }
        result.seed = candidate_queue.get(0);
        result.seedIdx = 0;
        result.mutate = candidate_queue.get(0).mutates.get(0);
        result.mutateIdx = 0;
        return result;
    }

    public static List<QueuePair> retrieveAPairList(List<QueueEntry> candidate_queue, Conf conf) {
        List<QueuePair> result = new ArrayList<QueuePair>();
        List<QueueEntry> queue = new ArrayList<QueueEntry>();
        for (QueueEntry seed: candidate_queue) {
            for (QueueEntry m:seed.mutates) {
                queue.add(m);
            }
        }

        int k = conf.FAULT_SEQUENCE_BATCH_SIZE;
        for (int i = 0; i < Math.min(queue.size(), k); i++) {
            QueueEntry entry = queue.get(i);
            QueuePair pair = new QueuePair();
            pair.seed = entry.father;
            pair.seedIdx = candidate_queue.indexOf(pair.seed);
            pair.mutate = entry;
            pair.mutateIdx = pair.seed.mutates.indexOf(pair.mutate);
            result.add(pair);
        }

        return result;
    }
}
