package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;

public class QueueEntry {
	public String fname; //file name for the queue entry
	public String seed; 
	public int len; //fault sequence length
	public FaultSequence faultSeq;
	public List<IOPoint> ioSeq;
	public int candidate_io;
	public int max_match_fault;
	
	public boolean was_tested; //Had been tested at least for one time
	
	public boolean was_fuzzed; //Had any fuzzing done yet?
	public int fuzzed_time; //count to retrieve it from the queue
	
	public QueueEntry() {
		mutates = new ArrayList<QueueEntry>();
		on_recovery_mutates = new ArrayList<QueueEntry>();
		favored_mutates = new ArrayList<QueueEntry>();
		unique_io_id = new HashSet<Integer>();
		recovery_io_id = new HashSet<Integer>();
		not_tested_fault_id = new HashSet<Integer>();
	}

	public List<QueueEntry> mutates;
	public List<QueueEntry> on_recovery_mutates;
	public List<QueueEntry> favored_mutates;
	
	public Set<Integer> unique_io_id;
	public Set<Integer> recovery_io_id;
	public Set<Integer> not_tested_fault_id;
	public boolean has_new_cov;                    /* Triggers new coverage?           */
	public boolean favored;        //gy for mutate favored                /* Currently favored?               */
	public boolean fs_redundant;                   /* Marked as redundant in the fs?   */
    
	public int bitmap_size;                    /* Number of bits set in bitmap     */
	public int exec_cksum;                     /* Checksum of the execution trace  */
	public int new_cov_contribution; 

	public long exec_s;                        /* Execution time (seconds)              */
	public int handicap;                       /* Number of queue cycles behind    */
	public long depth;                          /* Path depth                       */
    
	public QueueEntry next;           /* Next element, if any             */
	public QueueEntry next_100;       /* 100 elements ahead               */
    
    public void calibrate() {
    	this.max_match_fault = 0;
    	this.candidate_io = 0;
		if(this.faultSeq == null || this.faultSeq.isEmpty()) {
			this.faultSeq = new FaultSequence();
			this.faultSeq.curFault.set(-1);;
			this.faultSeq.seq = new ArrayList<FaultPoint>();
		} else {
			//fix faultSeq
			//TODO: current comparison approch could cause problems
			//fault node in fault sequence should match the real node in io sequence
			for(; (this.candidate_io < this.ioSeq.size()) && this.max_match_fault<this.faultSeq.seq.size(); ) {
				if(this.ioSeq.get(this.candidate_io).CALLSTACK.toString().equals(
						this.faultSeq.seq.get(this.max_match_fault).ioPt.CALLSTACK.toString())
						&& this.ioSeq.get(this.candidate_io).appearIdx == this.faultSeq.seq.get(this.max_match_fault).ioPt.appearIdx) {
					this.faultSeq.seq.get(this.max_match_fault).ioPt = this.ioSeq.get(this.candidate_io);
					this.faultSeq.seq.get(this.max_match_fault).tarNodeIp = this.faultSeq.seq.get(this.max_match_fault).actualNodeIp;
					this.faultSeq.seq.get(this.max_match_fault).actualNodeIp = null;
					this.max_match_fault++;
				}
				this.candidate_io++;
			}
			this.ioSeq.subList(this.candidate_io, this.ioSeq.size());
			this.candidate_io = 0;
		}
		
		this.faultSeq.reset();
    }
    
    /* Calculate case desirability score to adjust the length of havoc fuzzing.
    A helper function for fuzz_one(). Maybe some of these constants should
    go into config.h. */
    public int getPerfScore() {
        int avg_exec_us = FuzzInfo.total_execs==0?0:(int) (FuzzInfo.exec_us / FuzzInfo.total_execs);
        int avg_bitmap_size = (int) (FuzzInfo.total_bitmap_size / FuzzInfo.total_bitmap_entries);
        int perf_score = 100;
        
        /* Adjust score based on execution speed of this path, compared to the
        global average. Multiplier ranges from 0.1x to 3x. Fast inputs are
        less expensive to fuzz, so we're giving them more air time. */

     if (this.exec_s * 0.1 > avg_exec_us) perf_score = 10;
     else if (this.exec_s * 0.25 > avg_exec_us) perf_score = 25;
     else if (this.exec_s * 0.5 > avg_exec_us) perf_score = 50;
     else if (this.exec_s * 0.75 > avg_exec_us) perf_score = 75;
     else if (this.exec_s * 4 < avg_exec_us) perf_score = 300;
     else if (this.exec_s * 3 < avg_exec_us) perf_score = 200;
     else if (this.exec_s * 2 < avg_exec_us) perf_score = 150;
     
     /* Adjust score based on bitmap size. The working theory is that better
     coverage translates to better targets. Multiplier from 0.25x to 3x. */

  if (this.bitmap_size * 0.3 > avg_bitmap_size) perf_score *= 3;
  else if (this.bitmap_size * 0.5 > avg_bitmap_size) perf_score *= 2;
  else if (this.bitmap_size * 0.75 > avg_bitmap_size) perf_score *= 1.5;
  else if (this.bitmap_size * 3 < avg_bitmap_size) perf_score *= 0.25;
  else if (this.bitmap_size * 2 < avg_bitmap_size) perf_score *= 0.5;
  else if (this.bitmap_size * 1.5 < avg_bitmap_size) perf_score *= 0.75;
  
  if (this.handicap >= 4) {

	    perf_score *= 4;
//	    this.handicap -= 4;

	  } else if (this.handicap>0) {

	    perf_score *= 2;
//	    this.handicap--;

	  }

	  /* Final adjustment based on input depth, under the assumption that fuzzing
	     deeper test cases is more likely to reveal stuff that can't be
	     discovered with traditional fuzzers. */

      int faults = this.faultSeq.seq.size();
      if(faults < 6 && faults > 0) {
    	  perf_score *= faults; 
      } else if(faults >= 6 && faults <9 ) {
    	  perf_score *= 3;
      } else if(faults >= 9 && faults <12) {
    	  perf_score *= 2;
      }
  
  if (perf_score > FuzzConf.HAVOC_MAX_MULT * 100) perf_score = FuzzConf.HAVOC_MAX_MULT * 100;

  return perf_score;
    }
}
