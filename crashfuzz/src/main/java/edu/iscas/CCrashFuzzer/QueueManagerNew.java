package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class QueueManagerNew {
	public static class QueuePair {
		public QueueEntry seed;
		public int seedIdx;
		public QueueEntry mutate;
		public int mutateIdx;
	}
	public static Set<Integer> tested_fault_id = new HashSet<Integer>();;
	public static QueuePair retrieveAnEntry(List<QueueEntry> candidate_queue) {
		if(candidate_queue == null ||candidate_queue.isEmpty()) {
			return null;
		}
		
	    Random rand = new Random();
	    int totalSum = 0;
	    
	    if(Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_TO_NEW_PROB) {
	    	Stat.log("Check entry in global not_tested");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
	    		for(QueueEntry m:q.mutates) {
    				FaultPoint lastFault = m.faultSeq.seq.get(m.faultSeq.seq.size()-1);
    				int id = (lastFault.ioPt.CALLSTACK+lastFault.stat.toString()+lastFault.tarNodeIp).hashCode();
					
    				if(!tested_fault_id.contains(id)) {
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
		        	for(j = 0; j<candidate_queue.get(i).mutates.size() && sum < index; j++) {
	    				FaultPoint lastFault = candidate_queue.get(i).mutates.get(j).faultSeq.seq.get(candidate_queue.get(i).mutates.get(j).faultSeq.seq.size()-1);
	    				int id = (lastFault.ioPt.CALLSTACK+lastFault.stat.toString()+lastFault.tarNodeIp).hashCode();
						
	    				if(!tested_fault_id.contains(id)) {
	    					sum = sum + candidate_queue.get(i).mutates.get(j).getPerfScore();
	    				}
	    			}
		            i++;
		        }
		        Stat.log("Retrieve entry in global not_tested_fault_id:"+Math.max(0,i-1)+":"+Math.max(0,j-1));
		    	
		    	if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap >= 4) {
		    		candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap -= 4;
		    	  } else if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap>0) {
		    		  candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap--;
		    	  }
		    	
		    	QueuePair pair = new QueuePair();
		    	pair.seedIdx = Math.max(0,i-1);
		    	pair.seed = candidate_queue.get(pair.seedIdx);
		    	pair.mutateIdx = Math.max(0,j-1);
		    	pair.mutate = pair.seed.mutates.get(pair.mutateIdx);
		    	
		        return pair;
		    }
	    }
	    
	    if(Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_NFAV_OLD_PROB) {
	    	Stat.log("Check entry on_recovery");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
		    	for(QueueEntry m:q.on_recovery_mutates) {
		    		if(m.faultSeq.on_recovery) {
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
		        	 for(j = 0; j<candidate_queue.get(i).on_recovery_mutates.size() && sum < index; j++) {
		        		 if(candidate_queue.get(i).on_recovery_mutates.get(j).faultSeq.on_recovery) {
		        			 sum = sum + candidate_queue.get(i).on_recovery_mutates.get(j).getPerfScore();
		        		 }
		        	 }
		             i++;
		        }
		        Stat.log("Retrieve entry in on_recovery:"+Math.max(0,i-1)+":"+Math.max(0,j-1));
		        int mutateIdx = candidate_queue.get(Math.max(0,i-1)).mutates.indexOf(candidate_queue.get(Math.max(0,i-1)).on_recovery_mutates.get(Math.max(0,j-1)));
		    	
		    	if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap >= 4) {
		    		candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap -= 4;
		    	  } else if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap>0) {
		    		  candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap--;
		    	  }
		    	
		    	QueuePair pair = new QueuePair();
		    	pair.seedIdx = Math.max(0,i-1);
		    	pair.seed = candidate_queue.get(pair.seedIdx);
		    	pair.mutateIdx = Math.max(0,mutateIdx);
		    	pair.mutate = pair.seed.mutates.get(pair.mutateIdx);
		    	Stat.log("Retrieve entry:"+pair.seedIdx+":"+pair.mutateIdx);
		        return pair;
		    }
	    }
	    
	    
	    if(Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_TO_OTHER_ENTRY_5) {
	    	Stat.log("Check entry in local not_tested_fault_id");
	    	totalSum = 0;
	    	for(QueueEntry q:candidate_queue) {
	    		if(!q.not_tested_fault_id.isEmpty()) {
	    			for(QueueEntry m:q.mutates) {
	    				FaultPoint lastFault = m.faultSeq.seq.get(m.faultSeq.seq.size()-1);
	    				int id = (lastFault.ioPt.CALLSTACK+lastFault.stat.toString()+lastFault.tarNodeIp).hashCode();
						
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
		    				int id = (lastFault.ioPt.CALLSTACK+lastFault.stat.toString()+lastFault.tarNodeIp).hashCode();
							
		    				if(candidate_queue.get(i).not_tested_fault_id.contains(id)) {
		    					sum = sum + candidate_queue.get(i).mutates.get(j).getPerfScore();
		    				}
		    			}
		    		}
		            i++;
		        }
		        Stat.log("Retrieve entry in not_tested_fault_id:"+Math.max(0,i-1)+":"+Math.max(0,j-1));
		    	
		    	if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap >= 4) {
		    		candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap -= 4;
		    	  } else if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap>0) {
		    		  candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap--;
		    	  }
		    	
		    	QueuePair pair = new QueuePair();
		    	pair.seedIdx = Math.max(0,i-1);
		    	pair.seed = candidate_queue.get(pair.seedIdx);
		    	pair.mutateIdx = Math.max(0,j-1);
		    	pair.mutate = pair.seed.mutates.get(pair.mutateIdx);
		    	
		        return pair;
		    }
	    }
	    
	    if(Fuzzer.getRandomNumber(100) < FuzzConf.SKIP_TO_OTHER_ENTRY_5) {
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
		    	
		    	if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap >= 4) {
		    		candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap -= 4;
		    	  } else if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap>0) {
		    		  candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,mutateIdx)).handicap--;
		    	  }
		    	
		    	QueuePair pair = new QueuePair();
		    	pair.seedIdx = Math.max(0,i-1);
		    	pair.seed = candidate_queue.get(pair.seedIdx);
		    	pair.mutateIdx = Math.max(0,mutateIdx);
		    	pair.mutate = pair.seed.mutates.get(pair.mutateIdx);

		        Stat.log("Retrieve entry:"+pair.seedIdx+":"+pair.mutateIdx);
		        return pair;
		    }
	    }

    	Stat.log("Check all the entries");
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
    	
    	if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap >= 4) {
    		candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap -= 4;
    	  } else if (candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap>0) {
    		  candidate_queue.get(Math.max(0,i-1)).mutates.get(Math.max(0,j-1)).handicap--;
    	  }
    	
    	QueuePair pair = new QueuePair();
    	pair.seedIdx = Math.max(0,i-1);
    	pair.seed = candidate_queue.get(pair.seedIdx);
    	pair.mutateIdx = Math.max(0,j-1);
    	pair.mutate = pair.seed.mutates.get(pair.mutateIdx);
    	
        return pair;
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
