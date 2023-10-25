package edu.iscas.tcse.faultfuzz.ctrl;

public class FuzzConf {
    /* Probabilities of skipping non-favored entries in the queue, expressed as
    percentages: */

    public static final int SKIP_TO_NEW_PROB = 99; /* ...when there are new, pending favorites */
    public static final int SKIP_NFAV_OLD_PROB = 95; /* ...no new favs, cur entry already fuzzed */
    public static final int  SKIP_NFAV_NEW_PROB = 75; /* ...no new favs, cur entry not fuzzed yet */
    
    public static final int  SKIP_TO_OTHER_ENTRY_5 = 90; /* ...no new covs for 0.5 progress */
    public static final int  SKIP_TO_OTHER_ENTRY_4 = 75; /* ...no new covs for 0.4 progress */
    public static final int  SKIP_TO_OTHER_ENTRY_3 = 60; /* ...no new covs for 0.3 progress */
    public static final int  SKIP_TO_OTHER_ENTRY_2 = 45; /* ...no new covs for 0.2 progress */
    public static final int  SKIP_TO_OTHER_ENTRY_1 = 30; /* ...no new covs for 0.1 progress */
    
    /* Maximum multiplier for the above (should be a power of two, beware
    of 32-bit int overflows): */
    public static final int  HAVOC_MAX_MULT = 16;
}
