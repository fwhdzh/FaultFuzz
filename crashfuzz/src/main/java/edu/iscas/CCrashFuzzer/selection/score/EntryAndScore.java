package edu.iscas.CCrashFuzzer.selection.score;

import java.util.List;
import java.util.Random;

import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;

class EntryAndScore implements Comparable<EntryAndScore> {
    static Random rand = new Random();
    
    	public QueueEntry entry;
    	public long score;
    	public EntryAndScore(QueueEntry entry, long score) {
    		this.entry = entry;
    		this.score = score;
    	}
    	@Override
    	public int compareTo(EntryAndScore o) {
    		// TODO Auto-generated method stub
            long r = this.score - o.score;
    		int result = r == 0? 0 : (int) (r / Math.abs(r));
    		// result = 0 - result;
    		return result;
    	}

    	public static void logScoresList(String title, List<EntryAndScore> list) {
    	    String logInfo = title + " score: {";
    	    for (int i = 0; i < list.size(); i++) {
    	        logInfo = logInfo + i + ":" + list.get(i).score + ",";
    	    }
    	    logInfo = logInfo + "}";
    	    Stat.log(EntryAndScore.class , logInfo);
    	}
}
