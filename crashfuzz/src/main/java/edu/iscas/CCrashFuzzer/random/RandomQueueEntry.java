package edu.iscas.CCrashFuzzer.random;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;

public class RandomQueueEntry {
	String fname; //file name for the queue entry
	public RandomFaultSequence faultSeq;
	
	public boolean was_tested; //Had been tested at least for one time
	
    boolean has_new_cov;                    /* Triggers new coverage?           */
    boolean favored;        //gy for mutate favored                /* Currently favored?               */
    
    int bitmap_size;                    /* Number of bits set in bitmap     */
    
    long exec_s;                        /* Execution time (seconds)              */
}
