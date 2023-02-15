package edu.iscas.tcse.favtrigger.triggering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPos;
import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultStat;

import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPoint;

public class CurrentFaultSequence {
	public static FaultSequence faultSeq;
    public static void loadCurrentCrashPoint(String cur_crash_path) {
    	try {
            faultSeq = new FaultSequence();
            faultSeq.seq = new ArrayList<FaultPoint>();
            faultSeq.curAppear = 0;
            faultSeq.curFault = -1;

    		File file = new File(cur_crash_path);
			if(!file.exists()) {
				return;
			}
			FileReader fileReader;
			fileReader = new FileReader(file);

            BufferedReader br = new BufferedReader(fileReader);
            String lineContent = null;
            FaultPoint p = null;
            while((lineContent = br.readLine()) != null){
            	String content = lineContent.substring(lineContent.indexOf("=")+1, lineContent.length()).trim();
            	if(lineContent.startsWith("fault point=")) {
            		p = new FaultPoint();
            		p.ioPt = new IOPoint();
            	} else if (lineContent.startsWith("event=")) {
            		if(content.trim().equals(FaultStat.CRASH.toString())) {
            			p.stat = FaultStat.CRASH;
            		} else if(content.trim().equals(FaultStat.REBOOT.toString())) {
            			p.stat = FaultStat.REBOOT;
            		}
            	} else if (lineContent.startsWith("pos=")) {
            		if(content.trim().equals(FaultPos.BEFORE.toString())) {
            			p.pos = FaultPos.BEFORE;
            		} else if(content.trim().equals(FaultPos.AFTER.toString())) {
            			p.pos = FaultPos.AFTER;
            		}
            	} else if(lineContent.startsWith("nodeIp=")) {
            		p.tarNodeIp = content.trim();
            	} else if(lineContent.startsWith("ioID=")) {
            		p.ioPt.ioID = Integer.parseInt(content.trim());
            	} else if (lineContent.startsWith("ioCallStack=")) {
            		List<String> callstack = new ArrayList<String>(Arrays.asList(content.substring(1, content.length()-1).split(", ")));
            		p.ioPt.CALLSTACK = callstack;
            	} else if (lineContent.startsWith("path=")) {
            		p.ioPt.PATH = content.trim();
            	} else if(lineContent.startsWith("ioAppearIdx=")) {
            		p.ioPt.appearIdx = Integer.parseInt(content.trim());
            	} else if(lineContent.equals("end")) {
            		faultSeq.seq.add(p);
            	}
	    	}
            if(faultSeq.seq.size()>0) {
            	faultSeq.curFault = 0;
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
