package edu.iscas.CCrashFuzzer;

import java.util.List;

public class QueueEntry {
	String fname; //file name for the queue entry
	int len; //fault sequence length
	FaultSequence faultSeq;
	List<IOPoint> ioSeq;
	int candidate_io;
	int max_match_fault;
	
	boolean was_fuzzed; //Had any fuzzing done yet?
	int left_mutates_last_test;
    boolean has_new_cov;                    /* Triggers new coverage?           */
    boolean favored;        //gy for mutate favored                /* Currently favored?               */
    boolean fs_redundant;                   /* Marked as redundant in the fs?   */
    
    int bitmap_size;                    /* Number of bits set in bitmap     */
    int exec_cksum;                     /* Checksum of the execution trace  */

    long exec_m;                        /* Execution time (minutes)              */
    long handicap;                       /* Number of queue cycles behind    */
    long depth;                          /* Path depth                       */
    
    boolean trace_mini;                     /* Trace bytes, if kept             */
    int tc_ref;                         /* Trace bytes ref count            */
    
    QueueEntry next;           /* Next element, if any             */
    QueueEntry next_100;       /* 100 elements ahead               */
    
    /* Calculate case desirability score to adjust the length of havoc fuzzing.
    A helper function for fuzz_one(). Maybe some of these constants should
    go into config.h. */
    public int getPerfScore() {
        int avg_exec_us = (int) (Fuzzer.total_exec_mins / Fuzzer.totalExecutions);
        int avg_bitmap_size = (int) (Fuzzer.total_bitmap_size / Fuzzer.total_bitmap_entries);
        int perf_score = 100;
        
        /* Adjust score based on execution speed of this path, compared to the
        global average. Multiplier ranges from 0.1x to 3x. Fast inputs are
        less expensive to fuzz, so we're giving them more air time. */

     if (this.exec_m * 0.1 > avg_exec_us) perf_score = 10;
     else if (this.exec_m * 0.25 > avg_exec_us) perf_score = 25;
     else if (this.exec_m * 0.5 > avg_exec_us) perf_score = 50;
     else if (this.exec_m * 0.75 > avg_exec_us) perf_score = 75;
     else if (this.exec_m * 4 < avg_exec_us) perf_score = 300;
     else if (this.exec_m * 3 < avg_exec_us) perf_score = 200;
     else if (this.exec_m * 2 < avg_exec_us) perf_score = 150;
     
     /* Adjust score based on bitmap size. The working theory is that better
     coverage translates to better targets. Multiplier from 0.25x to 3x. */

  if (this.bitmap_size * 0.3 > avg_bitmap_size) perf_score *= 3;
  else if (this.bitmap_size * 0.5 > avg_bitmap_size) perf_score *= 2;
  else if (this.bitmap_size * 0.75 > avg_bitmap_size) perf_score *= 1.5;
  else if (this.bitmap_size * 3 < avg_bitmap_size) perf_score *= 0.25;
  else if (this.bitmap_size * 2 < avg_bitmap_size) perf_score *= 0.5;
  else if (this.bitmap_size * 1.5 < avg_bitmap_size) perf_score *= 0.75;
  
  //ajust score according to the number of injected faults
  //TODO
  
  if (perf_score > FuzzConf.HAVOC_MAX_MULT * 100) perf_score = FuzzConf.HAVOC_MAX_MULT * 100;

  return perf_score;
    }
}
