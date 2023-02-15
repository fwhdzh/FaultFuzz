package edu.iscas.CCrashFuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class RecoveryManager {
	public void loadQueue(List<QueueEntry> candidate_queue, String path, Conf conf) {
		File dir = new File(path);
		if(!dir.exists()) {
			return;
		}
		File[] files = dir.listFiles();
		
		for(File f:files) {
			if(f.isDirectory()) {
				QueueEntry new_q = new QueueEntry();
				new_q.fname = f.getName();
				new_q.ioSeq = null;
				TraceReader reader = new TraceReader(f.getAbsolutePath()+"/"+FileUtil.ioTracesDir);
				reader.readTraces();
				new_q.ioSeq = reader.ioPoints;
				FaultSequence faults = loadCurrentCrashPoint(f.getAbsolutePath()+"/"+conf.CUR_CRASH_FILE.getName());
				new_q.faultSeq = faults;
				try {
					FileInputStream in = new FileInputStream(f.getAbsoluteFile()+"/"+FileUtil.exec_second_file);
					byte[] content = new byte[1024];
					in.read(content);
					String number = (new String(content)).trim();
					new_q.exec_s = FileUtil.parseStringTimeToSeconds(number);
					in.close();
					
					in = new FileInputStream(f.getAbsoluteFile()+"/"+FileUtil.traced_size_file);
					Arrays.fill(content, (byte)0);
					in.read(content);
					number = (new String(content)).trim();
					new_q.bitmap_size = Integer.parseInt(number);
					in.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
				new_q.calibrate();
				
				candidate_queue.add(new_q);
			}
		}
	}
	public void loadFuzzed(Set<String> fuzzedFiles, String path, Conf conf) {
		File dir = new File(path);
		if(!dir.exists()) {
			return;
		}
		File[] files = dir.listFiles();
		
		for(File f:files) {
			if(f.isDirectory()) {
				File[] contents = f.listFiles();
				for(File fuzzed:contents) {
					if(fuzzed.isFile() && fuzzed.getName().equals(conf.CUR_CRASH_FILE.getName())
							&& loadCurrentCrashPoint(fuzzed.getAbsolutePath()) != null) {
						fuzzedFiles.add(f.getName());
					}
				}
			}
		}
	}
    public FaultSequence loadCurrentCrashPoint(String cur_crash_path) {
		FaultSequence faultSeq = new FaultSequence();
    	try {

    		File file = new File(cur_crash_path);
			if(!file.exists()) {
				return null;
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
            faultSeq.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        return faultSeq;
    }
}
