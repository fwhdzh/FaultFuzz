package edu.iscas.tcse.faultfuzz.ctrl.selection;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;

public class FIFOQueueEntrySelector {
    public static SelectionInfo.QueuePair retrieveAnEntry(List<QueueEntry> candidate_queue) {
        SelectionInfo.QueuePair result = new SelectionInfo.QueuePair();;
        if (candidate_queue == null || candidate_queue.size() == 0) {
            return null;
        }
        result.seed = candidate_queue.get(0);
        result.seedIdx = 0;
        result.mutate = candidate_queue.get(0).mutates.get(0);
        result.mutateIdx = 0;
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

        int k = conf.FAULT_SEQUENCE_BATCH_SIZE;
        for (int i = 0; i < Math.min(queue.size(), k); i++) {
            QueueEntry entry = queue.get(i);
            SelectionInfo.QueuePair pair = new SelectionInfo.QueuePair();
            pair.seed = entry.seed;
            pair.seedIdx = candidate_queue.indexOf(pair.seed);
            pair.mutate = entry;
            pair.mutateIdx = pair.seed.mutates.indexOf(pair.mutate);
            result.add(pair);
        }

        return result;
    }
}
