package edu.iscas.CCrashFuzzer;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class QueueManager {
	public static QueueEntry retrieveAnEntry(List<QueueEntry> candidate_queue) {
	    Random rand = new Random();
	    int totalSum = 0;
	    for(QueueEntry q:candidate_queue) {
	        totalSum += q.getPerfScore();
	    }
	    
	    int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        while(sum < index ) {
             sum = sum + candidate_queue.get(i).getPerfScore();
             i++;
        }
        Stat.log("Retrieve entry:"+Math.max(0,i-1));
    	
    	if (candidate_queue.get(Math.max(0,i-1)).handicap >= 4) {
    	    candidate_queue.get(Math.max(0,i-1)).handicap -= 4;
    	  } else if (candidate_queue.get(Math.max(0,i-1)).handicap>0) {
    		candidate_queue.get(Math.max(0,i-1)).handicap--;
    	  }
    	
        return candidate_queue.get(Math.max(0,i-1));
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
				int rand_num = Fuzzer.getRandomNumber(100);
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
				         Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
					continue;
				}
			} else if (!tmp.favored) {
				int rand_num = Fuzzer.getRandomNumber(100);
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
			         Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
				continue;
			}
			
			if(favored == 0) {
				int rand_num = Fuzzer.getRandomNumber(100);
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
}
