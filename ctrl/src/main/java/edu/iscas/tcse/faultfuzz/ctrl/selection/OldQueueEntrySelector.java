package edu.iscas.tcse.faultfuzz.ctrl.selection;

import java.util.List;
import java.util.Random;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.FuzzConf;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.selection.score.ScoreQueueEntrySelector;
import edu.iscas.tcse.faultfuzz.ctrl.traditionalworkflow.TranditionalFuzzingMutationSelector;

public class OldQueueEntrySelector {
	static Random rand = new Random();


	public static SelectionInfo.QueuePair retrieveAnEntry(List<QueueEntry> candidate_queue) {
		if(candidate_queue == null ||candidate_queue.isEmpty()) {
			return null;
		}
		SelectionInfo.QueuePair result = null;
		result = tryToGetAQueueEntryWithGlobalNewPoint(candidate_queue);
		if (result != null) return result;
		result = tryToGetAQueueEntryWithRecovery(candidate_queue);
		if (result != null) return result;
		result = tryToGetAQueueEntryWithLocalNewPoint(candidate_queue);
	    if (result != null) return result;
	    result = tryToGetAQueueEntryWithFavoredMutates(candidate_queue);
		if (result != null) return result;
    	Stat.log("Check all the entries");
		result = tryToGetAQueueEntryWithRandom(candidate_queue);
		if (result != null) return result;
		return result;
	}

	public static List<SelectionInfo.QueuePair> retrievePairListInFAVFuzzingProcess(List<QueueEntry> candidate_queue, Conf conf) {
		// List<QueuePair> result = new ArrayList<QueuePair>();
		// QueuePair p = retrieveAnEntry(candidate_queue);
		// if (p != null) {
		// 	result.add(p);
		// }
		List<SelectionInfo.QueuePair> result = ScoreQueueEntrySelector.retrieveAPairList(candidate_queue, conf);
		return result;
	}

	// public static class EntryAndScoreSelection {
	// 	public QueueEntry entry;
	// 	public int score;
	// 	public EntryAndScoreSelection(QueueEntry entry, int score) {
	// 		this.entry = entry;
	// 		this.score = score;
	// 	}
	// }

	public static SelectionInfo.QueuePair tryToGetAQueueEntryWithGlobalNewPoint(List<QueueEntry> candidate_queue) {
		SelectionInfo.QueuePair result = null;
		int totalSum = 0;
	    if(OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
	    	Stat.log("Check entry in global not_tested");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
	    		for(QueueEntry m:q.mutates) {
					
					if (SelectionInfo.checkIfEntryIsGlobalNewIO(m)) {
						totalSum += m.getPerfScore();
					}

    				// FaultPoint lastFault = m.faultSeq.seq.get(m.faultSeq.seq.size()-1);
					// int id = lastFault.getFaultID();
					
    				// if(!tested_fault_id.contains(id)) {
    				// 	totalSum += m.getPerfScore();
    				// }
    			}
		    }
		    Stat.log("totalSum:"+totalSum);
		    if(totalSum != 0) {
		    	int index = rand.nextInt(totalSum);
		        int sum = 0;
		        int i=0;
		        int j = 0;
		        while(sum < index ) {
		        	for(j = 0; j<candidate_queue.get(i).mutates.size() && sum < index; j++) {

						QueueEntry e = candidate_queue.get(i).mutates.get(j);
						if (SelectionInfo.checkIfEntryIsGlobalNewIO(e)) {
							sum = sum + e.getPerfScore();
						}

	    				// FaultPoint lastFault = candidate_queue.get(i).mutates.get(j).faultSeq.seq.get(candidate_queue.get(i).mutates.get(j).faultSeq.seq.size()-1);
						// int id = lastFault.getFaultID();
						
	    				// if(!tested_fault_id.contains(id)) {
	    				// 	sum = sum + candidate_queue.get(i).mutates.get(j).getPerfScore();
	    				// }
	    			}
		            i++;
		        }
		        Stat.log("Retrieve entry in global not_tested_fault_id:"+Math.max(0,i-1)+":"+Math.max(0,j-1));

				int seedIdx = Math.max(0,i-1);
				int mutateIdx = Math.max(0,j-1);
				Stat.log("Retrieve entry in global not_tested_fault_id:"+seedIdx+":"+mutateIdx);
		        result = updateHandicapAndConstructQueuePair(candidate_queue, seedIdx, mutateIdx);
		    }
	    }
		return result;
	}

	private static SelectionInfo.QueuePair updateHandicapAndConstructQueuePair(List<QueueEntry> candidate_queue, int seedIdx, int mutateIdx) {
		SelectionInfo.QueuePair result = null;
		if (candidate_queue.get(seedIdx).mutates.get(mutateIdx).handicap >= 4) {
			candidate_queue.get(seedIdx).mutates.get(mutateIdx).handicap -= 4;
		} else if (candidate_queue.get(seedIdx).mutates.get(mutateIdx).handicap > 0) {
			candidate_queue.get(seedIdx).mutates.get(mutateIdx).handicap--;
		}
		
		result = new SelectionInfo.QueuePair();
		result.seedIdx = seedIdx;
		result.seed = candidate_queue.get(result.seedIdx);
		result.mutateIdx = mutateIdx;
		result.mutate = result.seed.mutates.get(result.mutateIdx);
		return result;
	}

	private static SelectionInfo.QueuePair tryToGetAQueueEntryWithRecovery(List<QueueEntry> candidate_queue) {
		SelectionInfo.QueuePair result = null;
		int totalSum = 0;
		if(OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_NFAV_OLD_PROB) {
	    	Stat.log("Check entry on_recovery");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
		    	for(QueueEntry m:q.on_recovery_mutates) {

					if (TranditionalFuzzingMutationSelector.checkIfEntryIsRecovery(m)) {
						totalSum += m.getPerfScore();
					}

		    		// if(m.faultSeq.on_recovery) {
		    		// 	totalSum += m.getPerfScore();
		    		// }
		    	}
		    }
		    
		    if(totalSum != 0) {
		    	int index = rand.nextInt(totalSum);
		        int sum = 0;
		        int i=0;
		        int j = 0;
		        while(sum < index ) {
		        	 for(j = 0; j<candidate_queue.get(i).on_recovery_mutates.size() && sum < index; j++) {

						QueueEntry e = candidate_queue.get(i).on_recovery_mutates.get(j);
						if (TranditionalFuzzingMutationSelector.checkIfEntryIsRecovery(e)) {
							sum = sum + e.getPerfScore();
						}

		        		//  if(candidate_queue.get(i).on_recovery_mutates.get(j).faultSeq.on_recovery) {
		        		// 	 sum = sum + candidate_queue.get(i).on_recovery_mutates.get(j).getPerfScore();
		        		//  }
		        	 }
		             i++;
		        }
		        Stat.log("Retrieve entry in on_recovery:"+Math.max(0,i-1)+":"+Math.max(0,j-1));
		        int mutateIdx = candidate_queue.get(Math.max(0,i-1)).mutates.indexOf(candidate_queue.get(Math.max(0,i-1)).on_recovery_mutates.get(Math.max(0,j-1)));
		    	
				int seedIdx = Math.max(0,i-1);
				int realMutateIdx = Math.max(0,mutateIdx);
				result = updateHandicapAndConstructQueuePair(candidate_queue, seedIdx, realMutateIdx);
		    	Stat.log("Retrieve entry:"+result.seedIdx+":"+result.mutateIdx);
		    }
	    }
		return result;
	}

	private static SelectionInfo.QueuePair tryToGetAQueueEntryWithLocalNewPoint(List<QueueEntry> candidate_queue) {
		SelectionInfo.QueuePair result = null;
		int totalSum = 0;
		if(OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_TO_OTHER_ENTRY_5) {
	    	Stat.log("Check entry in local not_tested_fault_id");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
	    		if(!q.not_tested_fault_id.isEmpty()) {
	    			for(QueueEntry m:q.mutates) {
	    				FaultPoint lastFault = m.faultSeq.seq.get(m.faultSeq.seq.size()-1);
						int id = lastFault.getFaultID();
	    				if(q.not_tested_fault_id.contains(id)) {
	    					totalSum += m.getPerfScore();
	    				}
	    			}
	    		}
		    }
		    
		    if(totalSum != 0) {
		    	int index = rand.nextInt(totalSum);
		        int sum = 0;
		        int i=0;
		        int j = 0;
		        while(sum < index ) {
		        	if(!candidate_queue.get(i).not_tested_fault_id.isEmpty()) {
		        		for(j = 0; j<candidate_queue.get(i).mutates.size() && sum < index; j++) {
		    				FaultPoint lastFault = candidate_queue.get(i).mutates.get(j).faultSeq.seq.get(candidate_queue.get(i).mutates.get(j).faultSeq.seq.size()-1);
							int id = lastFault.getFaultID();
		    				if(candidate_queue.get(i).not_tested_fault_id.contains(id)) {
		    					sum = sum + candidate_queue.get(i).mutates.get(j).getPerfScore();
		    				}
		    			}
		    		}
		            i++;
		        }
		        Stat.log("Retrieve entry in not_tested_fault_id:"+Math.max(0,i-1)+":"+Math.max(0,j-1));

				int seedIdx = Math.max(0,i-1);
				int mutateIdx = Math.max(0,j-1);
				result = updateHandicapAndConstructQueuePair(candidate_queue, seedIdx, mutateIdx);
		    }
	    }
		return result;
	}

	private static SelectionInfo.QueuePair tryToGetAQueueEntryWithFavoredMutates(List<QueueEntry> candidate_queue) {
		SelectionInfo.QueuePair result = null;
		int totalSum = 0;
		if(OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_TO_OTHER_ENTRY_5) {
	    	Stat.log("Check favored entries");
	    	totalSum = 0;
		    
		    for(QueueEntry q:candidate_queue) {
		    	for(QueueEntry m:q.favored_mutates) {
		    		if(m.favored) {
		    			totalSum += m.getPerfScore();
		    		}
		    	}
		    }
		    if(totalSum != 0) {
		    	int index = rand.nextInt(totalSum);
		        int sum = 0;
		        int i=0;
		        int j = 0;
		        while(sum < index ) {
		        	 for(j = 0; j<candidate_queue.get(i).favored_mutates.size() && sum < index; j++) {
		        		 if(candidate_queue.get(i).favored_mutates.get(j).favored) {
		        			 sum = sum + candidate_queue.get(i).favored_mutates.get(j).getPerfScore();
		        		 }
		        	 }
		             i++;
		        }
		        Stat.log("Retrieve entry in favored:"+Math.max(0,i-1)+":"+Math.max(0,j-1));
		        int mutateIdx = candidate_queue.get(Math.max(0,i-1)).mutates.indexOf(candidate_queue.get(Math.max(0,i-1)).favored_mutates.get(Math.max(0,j-1)));
		    	
				int seedIdx = Math.max(0,i-1);
				int realMutateIdx = Math.max(0,mutateIdx);
				result = updateHandicapAndConstructQueuePair(candidate_queue, seedIdx, realMutateIdx);
		        Stat.log("Retrieve entry:"+result.seedIdx+":"+result.mutateIdx);
		    }
	    }
		return result;
	}

	private static SelectionInfo.QueuePair tryToGetAQueueEntryWithRandom(List<QueueEntry> candidate_queue) {
		SelectionInfo.QueuePair result = null;
		int totalSum = 0;
		totalSum = 0;
	    for(QueueEntry q:candidate_queue) {
	    	for(QueueEntry m:q.mutates) {
	    		totalSum += m.getPerfScore();
	    	}
	    }
	    int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        int j = 0;
        while(sum < index ) {
        	 for(j = 0; j<candidate_queue.get(i).mutates.size() && sum < index; j++) {
                 sum = sum + candidate_queue.get(i).mutates.get(j).getPerfScore();
        	 }
             i++;
        }
        Stat.log("Retrieve entry:"+Math.max(0,i-1)+":"+Math.max(0,j-1));

		int seedIdx = Math.max(0,i-1);
		int mutateIdx = Math.max(0,j-1);
		result = updateHandicapAndConstructQueuePair(candidate_queue, seedIdx, mutateIdx);
		Stat.log("Retrieve entry:"+result.seedIdx+":"+result.mutateIdx);
		return result;
	}
	
	public static QueueEntry retrieveAnEntrySimple(List<QueueEntry> candidate_queue) {
	    Random rand = new Random();
	    int totalSum = 0;
	    for(QueueEntry q:candidate_queue) {
	        totalSum += q.getPerfScore();
	    }
	    
	    int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        while(sum < index ) {
             sum = sum + candidate_queue.get(i++).getPerfScore();
        }
        Stat.log("Retrieve entry:"+Math.max(0,i-1));
//        return candidate_queue.get(Math.max(0,i-1));
        return candidate_queue.get(Math.max(0,i-1));
	    
//		if(queue_cur == null) {
//			return null;
//		} else {
//			return queue_cur;
//		}
	}
	
	public static int pickAMutation(QueueEntry q, List<QueueEntry> mutates, 
			int favored, int untested_io, int queue_cycle, double no_new_cov_pro) {
		int cur_mutate = -1;
		
		while(!mutates.isEmpty() && cur_mutate == -1) {
			int pick = -1;
			QueueEntry tmp = null;
			if(!q.on_recovery_mutates.isEmpty()) {
				tmp = q.on_recovery_mutates.get(0);
				pick = mutates.indexOf(tmp);
			}
			
			if(pick == -1) {
				Random r = new Random();
				pick = r.nextInt(mutates.size());
				tmp = mutates.get(pick);
			}
			
			if(favored> 0 || untested_io > 0 || !q.on_recovery_mutates.isEmpty()) {
				//long no new coverage
				int rand_num = OldQueueEntrySelector.getRandomNumber(100);
				if(no_new_cov_pro > 0.5 && rand_num < FuzzConf.SKIP_TO_OTHER_ENTRY_5) {
					return -1;
				} else if (no_new_cov_pro > 0.4 && rand_num < FuzzConf.SKIP_TO_OTHER_ENTRY_4) {
					return -1;
				} else if (no_new_cov_pro > 0.3 && rand_num < FuzzConf.SKIP_TO_OTHER_ENTRY_3) {
					return -1;
				} else if (no_new_cov_pro > 0.2 && rand_num < FuzzConf.SKIP_TO_OTHER_ENTRY_2) {
					return -1;
				} else if (no_new_cov_pro > 0.1 && rand_num < FuzzConf.SKIP_TO_OTHER_ENTRY_1) {
					return -1;
				}
				
				if((!tmp.favored || q.was_fuzzed) &&
				         OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
					continue;
				}
			} else if (!tmp.favored) {
				int rand_num = OldQueueEntrySelector.getRandomNumber(100);
				if (queue_cycle> 1 && !q.was_fuzzed && rand_num < FuzzConf.SKIP_NFAV_NEW_PROB) {
					return -1;
				} else if (rand_num < FuzzConf.SKIP_NFAV_OLD_PROB) {
					return -1;
	            }
			}
			
			cur_mutate = pick;
			break;
		}
		return cur_mutate;
	}
	
	public static int pickAMutationSimple(List<QueueEntry> mutates, int favored) {
		int cur_mutate = -1;
		while(!mutates.isEmpty() && cur_mutate == -1) {
			
			Random r = new Random();
			int pick = r.nextInt(mutates.size());
			
			//retrive a mutation or skip to queue
			QueueEntry tmp = mutates.get(pick);
			if(!tmp.favored && favored> 0 &&
			         OldQueueEntrySelector.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
				continue;
			}
			
			if(favored == 0) {
				int rand_num = OldQueueEntrySelector.getRandomNumber(100);
				if (rand_num < FuzzConf.SKIP_NFAV_NEW_PROB) {
					return -1;
				}

	            if (rand_num < FuzzConf.SKIP_NFAV_OLD_PROB) {
	            	continue;
	            }
			}
			
			cur_mutate = pick;
		}
		return cur_mutate;
	}

	//from 0 to limit-1
	public static int getRandomNumber(int limit) {
		int num = (int) (Math.random()*limit);
		return num;
	}
}
