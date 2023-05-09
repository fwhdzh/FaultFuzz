package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.Mutation.EntryAndScore;
import edu.iscas.CCrashFuzzer.QueueManagerNew.QueuePair;

public class QueueEntryPairSelection {

    // public static class ScoreField {
    //     public String desc;
    //     public int score;
    // } 

    public static class EntryAndScoreMap {
        public QueueEntry entry;
        public Map<String, Integer> scoreMap = new HashMap<>();

        public EntryAndScoreMap(QueueEntry entry) {
            this.entry = entry;
        }
    }

    
    public static class ScoreCombinedStrategy {
        public static void computeBasicSocreInMap(List<EntryAndScoreMap> list) {
            for(EntryAndScoreMap e:list) {
                e.scoreMap.put("basic", 100);
            }
        }
    
        public static void computePerfSocreInMap(List<EntryAndScoreMap> list) {
            for(EntryAndScoreMap e:list) {
                int perfScore = e.entry.father.getPerfScore();
                e.scoreMap.put("perf", perfScore);
            }
        }
    
        public static boolean checkIfEntryIsRecovery(QueueEntry entry) {
            boolean result = false;
            result = entry.faultSeq.on_recovery;
            return result;
        }
    
        public static void computRecoveryScoreInMap(List<EntryAndScoreMap> list) {
            for(EntryAndScoreMap e:list) {
                if (checkIfEntryIsRecovery(e.entry)) {
                    e.scoreMap.put("recovery", 5);
                } else {
                    e.scoreMap.put("recovery", 1);
                }
            }
        }
    
        public static void computeNodeSymmetrySocreInMap(List<EntryAndScoreMap> list) {
            for (EntryAndScoreMap es: list) {
                FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
                if (QueueManagerNew.tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                    es.scoreMap.put("nodeSymmetry", 20);
                } else {
                    es.scoreMap.put("nodeSymmetry", 1);
                }
            }
        }
    
        public static void computeCotinuesIPPointInOneNodeSocreInMap(List<EntryAndScoreMap> list) {
            for (EntryAndScoreMap es: list) {
                FaultPoint lastInjectFaultPoint = es.entry.faultSeq.seq.get(es.entry.faultSeq.seq.size()-1);
                if (es.entry.father.not_tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
                    es.scoreMap.put("cotinuesIPPointInOneNode", 1);
                } else {
                    es.scoreMap.put("cotinuesIPPointInOneNode", 5);
                }
            }
        }

        public static void logPartScores(String title, List<EntryAndScoreMap> list) {
		    String logInfo = title + " score: {";
		    for (int i = 0; i < list.size(); i++) {
		        logInfo = logInfo + i + ":" + "{" ;
                EntryAndScoreMap es = list.get(i);
                for (Entry<String, Integer> e: es.scoreMap.entrySet()) {
                    logInfo = logInfo + e.getKey() + ":" + e.getValue() + ",";
                }
                logInfo = logInfo + "},\n";
		    }
		    logInfo = logInfo + "}";
		    Stat.log(EntryAndScore.class , logInfo);
		}
    
        public static List<EntryAndScore> computeTotalScore(List<QueueEntry> queue) {
            List<EntryAndScore> result = new ArrayList<>();
            List<EntryAndScoreMap> list = new ArrayList<>();
            for (QueueEntry qe: queue) {
                EntryAndScoreMap emp = new EntryAndScoreMap(qe);
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
                EntryAndScoreMap emp = list.get(i);
                int basicSocre = emp.scoreMap.get("basic");
                int perfSocre = emp.scoreMap.get("perf");
                int recoverySocre = emp.scoreMap.get("recovery");
                int nodeSymmetrySocre = emp.scoreMap.get("nodeSymmetry");
                int cotinuesIPPointInOneNodeSocre = emp.scoreMap.get("cotinuesIPPointInOneNode");
                es.score = (basicSocre + perfSocre) * recoverySocre / nodeSymmetrySocre / cotinuesIPPointInOneNodeSocre;
                result.add(es);
            }
            
            return result; 
        }
    }
      

    @Deprecated
    public static class ScoreAddedStrategy {

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
                if (QueueManagerNew.tested_fault_id.contains(lastInjectFaultPoint.getFaultID())) {
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

    public static List<QueuePair> retrieveAPairList(List<QueueEntry> candidate_queue, Conf conf) {
        List<QueuePair> result = new ArrayList<QueuePair>();
        List<QueueEntry> queue = new ArrayList<QueueEntry>();
        for (QueueEntry seed: candidate_queue) {
            for (QueueEntry m:seed.mutates) {
                queue.add(m);
            }
        }

        // List<EntryAndScore> list = ScoreAddedStrategy.computeTotalScore(queue);
        List<EntryAndScore> list = ScoreCombinedStrategy.computeTotalScore(queue);

        EntryAndScore.logScoresList("retrieveAPairList", list);
        int k = conf.FAULT_SEQUENCE_BATCH_SIZE;
        List<QueueEntry> entryList = EntryAndScore.retriveAEntryListAndScoreBasedOnScore(list, k);
        for (QueueEntry m: entryList) {
            QueuePair pair = new QueuePair();
            pair.seed = m.father;
            pair.seedIdx = candidate_queue.indexOf(pair.seed);
            pair.mutate = m;
            pair.mutateIdx = pair.seed.mutates.indexOf(pair.mutate);
            result.add(pair);
        }
        return result;
    }
}
