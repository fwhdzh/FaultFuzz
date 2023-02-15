package edu.iscas.CCrashFuzzer;

public class FuzzConf {
    /* Probabilities of skipping non-favored entries in the queue, expressed as
    percentages: */

    static final int SKIP_TO_NEW_PROB = 99; /* ...when there are new, pending favorites */
    static final int SKIP_NFAV_OLD_PROB = 95; /* ...no new favs, cur entry already fuzzed */
    static final int  SKIP_NFAV_NEW_PROB = 75; /* ...no new favs, cur entry not fuzzed yet */
    
    static final int  SKIP_TO_OTHER_ENTRY_5 = 90; /* ...no new covs for 0.5 progress */
    static final int  SKIP_TO_OTHER_ENTRY_4 = 75; /* ...no new covs for 0.4 progress */
    static final int  SKIP_TO_OTHER_ENTRY_3 = 60; /* ...no new covs for 0.3 progress */
    static final int  SKIP_TO_OTHER_ENTRY_2 = 45; /* ...no new covs for 0.2 progress */
    static final int  SKIP_TO_OTHER_ENTRY_1 = 30; /* ...no new covs for 0.1 progress */
    
    /* Maximum multiplier for the above (should be a power of two, beware
    of 32-bit int overflows): */
    static final int  HAVOC_MAX_MULT = 16;
}
