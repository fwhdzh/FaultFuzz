package edu.iscas.tcse.faultfuzz.ctrl.selection.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;

public class ScoreCombinedStrategy {
    public static void computeBasicSocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for(EntryAndScore.EntryAndScoreMap e:list) {
            long basicSocre = 100;
            e.scoreMap.put("basic", basicSocre);
        }
    }

    public static void computePerfSocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for(EntryAndScore.EntryAndScoreMap e:list) {
            long perfScore = e.entry.seed.getPerfScore();
            e.scoreMap.put("perf", perfScore);
        }
    }

    public static boolean checkIfEntryIsRecovery(QueueEntry entry) {
        boolean result = false;
        result = entry.on_recovery;
        return result;
    }

    public static void computRecoveryScoreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for(EntryAndScore.EntryAndScoreMap e:list) {
            long recoveryScore = 1;
            if (checkIfEntryIsRecovery(e.entry)) {
                recoveryScore = 5;
            } else {
                recoveryScore = 1;
            }
            e.scoreMap.put("recovery", recoveryScore);
        }
    }

    public static void computeNodeSymmetrySocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for (EntryAndScore.EntryAndScoreMap es: list) {
            long nodeSymmetryScore = 1;
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
            if (SelectionInfo.tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                nodeSymmetryScore = 20;
            } else {
                nodeSymmetryScore = 1;
            }
            es.scoreMap.put("nodeSymmetry", nodeSymmetryScore);
        }
    }

    public static void computeCotinuesIPPointInOneNodeSocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for (EntryAndScore.EntryAndScoreMap es: list) {
            long cotinuesIPPointInOneNodeScore = 1;
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
            if (es.entry.seed.not_tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                cotinuesIPPointInOneNodeScore = 1;
            } else {
                cotinuesIPPointInOneNodeScore = 5;
            }
            es.scoreMap.put("cotinuesIPPointInOneNode", cotinuesIPPointInOneNodeScore);
        }
    }

    public static void logPartScores(String title, List<EntryAndScore.EntryAndScoreMap> list) {
	    String logInfo = title + " score: {";
	    for (int i = 0; i < list.size(); i++) {
	        logInfo = logInfo + i + ":" + "{" ;
            EntryAndScore.EntryAndScoreMap es = list.get(i);
            for (Entry<String, Long> e: es.scoreMap.entrySet()) {
                logInfo = logInfo + e.getKey() + ":" + e.getValue() + ",";
            }
            logInfo = logInfo + "},\n";
	    }
	    logInfo = logInfo + "}";
	    Stat.log(EntryAndScore.class , logInfo);
	}

    public static List<EntryAndScore> computeTotalScore(List<QueueEntry> queue) {
        List<EntryAndScore> result = new ArrayList<>();
        List<EntryAndScore.EntryAndScoreMap> list = new ArrayList<>();
        for (QueueEntry qe: queue) {
            EntryAndScore.EntryAndScoreMap emp = new EntryAndScore.EntryAndScoreMap(qe);
            list.add(emp);
        }
        // use list as args to give the possiblity to use global information
        computeBasicSocreInMap(list);
        computePerfSocreInMap(list);
        computRecoveryScoreInMap(list);
        computeNodeSymmetrySocreInMap(list);
        computeCotinuesIPPointInOneNodeSocreInMap(list);
        logPartScores("logPartScores", list);
        for (int i = 0; i < queue.size(); i++) {
            QueueEntry qe = queue.get(i);
            EntryAndScore es = new EntryAndScore(qe, 0);
            EntryAndScore.EntryAndScoreMap emp = list.get(i);
            long basicSocre = emp.scoreMap.get("basic");
            long perfSocre = emp.scoreMap.get("perf");
            long recoverySocre = emp.scoreMap.get("recovery");
            long nodeSymmetrySocre = emp.scoreMap.get("nodeSymmetry");
            long cotinuesIPPointInOneNodeSocre = emp.scoreMap.get("cotinuesIPPointInOneNode");
            es.score = (basicSocre + perfSocre) * recoverySocre / nodeSymmetrySocre / cotinuesIPPointInOneNodeSocre;
            result.add(es);
        }
        return result; 
    }
}