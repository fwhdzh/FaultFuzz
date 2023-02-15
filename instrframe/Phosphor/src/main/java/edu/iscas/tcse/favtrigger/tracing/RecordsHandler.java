package edu.iscas.tcse.favtrigger.tracing;

import edu.columbia.cs.psl.phosphor.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RecordsHandler {
    public static ConcurrentHashMap<String, ArrayList<FAVEntry>> traces = new ConcurrentHashMap<String, ArrayList<FAVEntry>>();
    public static ConcurrentHashMap<String, FileOutputStream> outs = new ConcurrentHashMap<String, FileOutputStream>();

    static {
        if(Configuration.RECORD_PHASE && Configuration.ASYC_TRACE) {
        	/*
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("start output traces to file");
                    for(String file:traces.keySet()) {
                        FileOutputStream out = outs.get(file);
                        if(out == null) {
                            continue;
                        }
                        try {
                            ArrayList<FAVEntry> entries = traces.get(file);
                            for(FAVEntry entry:entries) {
                                recordAnEntry(out, entry);
                            }
                            out.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });
            */
        }
    }
    public enum FaultPos {
		BEFORE,AFTER
	}
    public static void recordAnEntry(FileOutputStream out, FAVEntry entry) {
        try {
        	out.write((TraceItem.START.toString()).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.TIMESTAMP.toString()+":"+Long.toString(entry.TIMESTAMP)).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.THREADID.toString()+":"+Long.toString(entry.THREADID)).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.THREADOBJ.toString()+":"+Integer.toString(entry.THREADOBJ)).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.PATH.toString()+":"+entry.PATH).getBytes());
            RecordTaint.printLine(out);
            //out.write(b);
            //printLine(out);
            //out.write(md5.getBytes());
            out.write((TraceItem.FAULTPOS.toString()+":"+entry.recPosition).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.NEWCOV.toString()+":"+Integer.toString(entry.newCovs)).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.TAINT.toString()+":"+entry.TAINT.toString()).getBytes());
            RecordTaint.printLine(out);
            out.write((TraceItem.CALLSTACK.toString()+":"+entry.CALLSTACK.toString()).getBytes());
            RecordTaint.printLine(out);
        	out.write(TraceItem.END.toString().getBytes());
            RecordTaint.printLine(out);
            out.flush();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
    public static enum TraceItem {
		START,
		TIMESTAMP,
		THREADID,
		THREADOBJ,
		PATH,
		FAULTPOS,
		NEWCOV,
		TAINT,
		CALLSTACK,
		END
	}
}
