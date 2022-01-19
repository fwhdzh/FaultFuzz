package edu.iscas.CCrashFuzzer;

import java.util.List;

public class QueueEntry {
	String fname; //file name for the queue entry
	int len; //fault sequence length
	FaultSequence faultSeq;
	List<IOPoint> ioSeq;
	
	boolean was_fuzzed; //Had any fuzzing done yet?
    boolean has_new_cov;                    /* Triggers new coverage?           */
    boolean favored;                        /* Currently favored?               */
    boolean fs_redundant;                   /* Marked as redundant in the fs?   */
    
    int bitmap_size;                    /* Number of bits set in bitmap     */
    int exec_cksum;                     /* Checksum of the execution trace  */

    long exec_m;                        /* Execution time (minutes)              */
    long handicap;                       /* Number of queue cycles behind    */
    long depth;                          /* Path depth                       */

    QueueEntry next;           /* Next element, if any             */
    QueueEntry next_100;       /* 100 elements ahead               */
}
