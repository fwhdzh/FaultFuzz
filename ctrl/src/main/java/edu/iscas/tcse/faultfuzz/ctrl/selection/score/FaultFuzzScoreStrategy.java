package edu.iscas.tcse.faultfuzz.ctrl.selection.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;
import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;

public class FaultFuzzScoreStrategy {
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

    //TODO: has problem. The on_recovery filed is not correctly set.
    public static boolean checkIfEntryIsRecovery(QueueEntry entry) {
        boolean result = false;
        result = entry.on_recovery;
        return result;
    }

    public static void computRecoveryScoreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        for(EntryAndScore.EntryAndScoreMap e:list) {
            long recoveryScore = 0;
            if (checkIfEntryIsRecovery(e.entry)) {
                recoveryScore = 1;
            }
            e.scoreMap.put("recovery", recoveryScore);
        }
    }

    private static Map<String, Set<FaultPoint>> computeNodeToFaultsMap() {
        Map<String, Set<FaultPoint>> nodeToFaults = new HashMap<>();
        for (FaultPoint fp: SelectionInfo.testedFault) {
            String nodeIp = null;
            if (fp.type.equals(FaultType.CRASH) || fp.type.equals(FaultType.REBOOT)) {
                nodeIp = fp.tarNodeIp;
            }
            if (fp.type.equals(FaultType.NETWORK_DISCONNECTION) || fp.type.equals(FaultType.NETWORK_RECONNECTION)) {
                nodeIp = fp.params.get(0);
            }
            if (nodeIp == null) continue;
            nodeToFaults.computeIfAbsent(nodeIp, k -> new HashSet<FaultPoint>());
            nodeToFaults.get(nodeIp).add(fp);
        }
        return nodeToFaults;
    }

    public static void computeNodeSymmetrySocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        Map<String, Set<FaultPoint>> nodeToFaults = computeNodeToFaultsMap();
        for (EntryAndScore.EntryAndScoreMap es: list) {
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
            boolean symmetry = false;
            for (Entry<String, Set<FaultPoint>> entry: nodeToFaults.entrySet()) {
                if (!entry.getKey().equals(lastInjectFaultPoint.ioPt.ip)) {
                    Set<FaultPoint> nodeFPSet = entry.getValue();
                    for (FaultPoint fp: nodeFPSet) {
                        if (fp.getFaultIDWithOutIPInfo() == lastInjectFaultPoint.getFaultIDWithOutIPInfo()) {
                            symmetry = true;
                            break;
                        }
                    }
                }
            } 
            long nodeSymmetryScore = 0;
            if (symmetry) {
                nodeSymmetryScore = 1;
            }
            es.scoreMap.put("nodeSymmetry", nodeSymmetryScore);
        }
    }

    private static int timeWindowBound = 2000;

    public static void computelocalLoopSocreInMap(List<EntryAndScore.EntryAndScoreMap> list) {
        Map<String, Set<FaultPoint>> nodeToFaults = computeNodeToFaultsMap();
        for (EntryAndScore.EntryAndScoreMap es : list) {
            FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size() - 1);
            boolean isLocalLoop = false;
            Set<FaultPoint> localFaultPoints = nodeToFaults.getOrDefault(lastInjectFaultPoint.ioPt.ip, new HashSet<>());
            for (FaultPoint fp : localFaultPoints) {
                if (fp.getFaultIDWithOutIPInfo() == lastInjectFaultPoint.getFaultIDWithOutIPInfo()
                        && (Math.abs(fp.ioPtIdx - lastInjectFaultPoint.ioPtIdx) < timeWindowBound)) {
                    isLocalLoop = true;
                    break;
                }
            }
            long localLoopScore = 0;
            if (isLocalLoop) {
                localLoopScore = 1;
            }
            es.scoreMap.put("localLoop", localLoopScore);
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
	    Stat.debug(EntryAndScore.class , logInfo);
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
        // computeCotinuesIPPointInOneNodeSocreInMap(list);
        computelocalLoopSocreInMap(list);
        logPartScores("logPartScores", list);
        for (int i = 0; i < queue.size(); i++) {
            QueueEntry qe = queue.get(i);
            EntryAndScore es = new EntryAndScore(qe, 0);
            EntryAndScore.EntryAndScoreMap emp = list.get(i);
            long basicSocre = emp.scoreMap.get("basic");
            long nodeSymmetrySocre = emp.scoreMap.get("nodeSymmetry");
            long localLoopSocre = emp.scoreMap.get("localLoop");
            long recoverySocre = emp.scoreMap.get("recovery");
            long perfSocre = emp.scoreMap.get("perf");
            
            long nodeSymmetryScoreRatio = 100;
            long localLoopScoreRatio = 100;
            long recoveryScoreRatio = 100;
            long perfScoreRatio = 1;
            
            es.score = basicSocre +
                    nodeSymmetryScoreRatio * nodeSymmetrySocre +
                    localLoopScoreRatio * localLoopSocre +
                    recoveryScoreRatio * recoverySocre +
                    perfScoreRatio * perfSocre;
                    
            // es.score = (basicSocre + perfSocre) * list.size() * recoverySocre / nodeSymmetrySocre / localLoopSocre;
            
            if (es.score <= 0) {
                es.score = 1;
            }
            result.add(es);
        }
        return result; 
    }
}
