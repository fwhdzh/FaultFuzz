package edu.iscas.tcse.faultfuzz.ctrl.selection.score;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;

public class WorkloadSocreStrategy {

    public static void computeWorkloadSocre(List<EntryAndScore.EntryAndScoreMap> list) {
        for(EntryAndScore.EntryAndScoreMap e:list) {
            long workloadScore = 1;
            if (e.entry.workload.equals(Conf.currentWorkload)) {
                workloadScore = 0;
            }
            e.scoreMap.put("workload", workloadScore);
        }
    }

    public static List<EntryAndScore> computeTotalScore(List<QueueEntry> queue) {
        List<EntryAndScore> result = FaultFuzzScoreStrategy.computeTotalScore(queue);
        List<EntryAndScore.EntryAndScoreMap> list = new ArrayList<>();
        for (QueueEntry qe: queue) {
            EntryAndScore.EntryAndScoreMap emp = new EntryAndScore.EntryAndScoreMap(qe);
            list.add(emp);
        }
        computeWorkloadSocre(list);
        for (int i = 0; i < list.size(); i++) {
            EntryAndScore.EntryAndScoreMap emp = list.get(i);
            EntryAndScore es = result.get(i);
            long workloadScore = emp.scoreMap.get("workload");
            long workloadRatio = 100;
            es.score = es.score + workloadRatio * workloadScore;
        }
        return result;
    }
}
