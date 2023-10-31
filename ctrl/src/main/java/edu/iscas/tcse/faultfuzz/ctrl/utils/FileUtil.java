package edu.iscas.tcse.faultfuzz.ctrl.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.EntryConstructor;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPos;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class FileUtil {
	public final static String fuzzer_id_file = "faultfuzz_proc_id";
	
	public static int newBugFileWindow = 30; //minutes
	public static String root = "faultfuzz/";
	public static String root_tested = root+"tested/";//create a new file every "newBugFileWindow" miniues.
	public static String root_queue = root+"queue/";
	public static String root_fuzzed = root+"fuzzed/";
	public static String root_skipped = root+"skipped/";
	public static String root_non_triggered = root+"miss/";
	public static String root_bugs = root+"bugs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_hangs = root+"hangs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_tmp = root+"tmp/";

	public static String root_persist = root + "persist/";
	
	public final static String monitorDir = "monitor";
	public final static String ioTracesDir = "fav-rst";
	public final static String coverageDir = "cov";

	public final static String seed_file = "SEED";
	
	public final static String fuzzed_time_file = "FUZZED_TIME";
	public final static String mutates_size_file = "MUTATES_SIZE";
	public final static String handicap_file = "HANDICAP";
	public final static String mutates_file = "MUTATES";
	
	public final static String exec_second_file = "EXEC_TIME";
	public final static String traced_size_file = "TRACE_SIZE";
	
	public final static String total_execution_file = "TOTAL_EXEC_NUM";
	public final static String total_map_entry_file = "TOTAL_MAP_ENTRY";
	
	public final static String map_file = "MAP";
	public final static String virgin_map_file = "VIRGIN_MAP";
	public final static String virgin_map_size_file = "VIRGIN_MAP_SIZE";
	
	public final static String report_file = "TEST_REPORT";
	
	public final static String total_tested_time = "TESTED_TIME";

	public final static String faultSeqFile = "FAULT_SEQ";
	public final static String faultSeqJSONFile = "FAULT_SEQ_JSON";

	public final static String pause_file = "PAUSE";
	
	public static void init(String _root) {
		root = _root;
		root_tested = root+"tested/";//create a new file every "newBugFileWindow" miniues.
		root_queue = root+"queue/";
		root_fuzzed = root+"fuzzed/";
		root_skipped = root+"skipped/";
		root_non_triggered = root+"miss/";
		root_bugs = root+"bugs/";//create a new file every "newBugFileWindow" miniues.
		root_hangs = root+"hangs/";//create a new file every "newBugFileWindow" miniues.
		root_tmp = root+"tmp/";

		root_persist = root + "persist/";
	}
	
	public static void generateFAVLogInfo(String seed, String testID, List<String> logInfo, FaultSequence seq) {
		String rootReport = FileUtil.root_tmp+testID+"/"+"fuzz.log";

        try {
            File tofile = new File(rootReport);

            if (!tofile.getParentFile().exists()) {
                tofile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(tofile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println("Fault sequence info {");
            pw.println(seq.toString());
            pw.println("}");
            pw.println("FAVLog info: ");
            for(String s :logInfo) {
                pw.println(s);
            }
            pw.println("");

            pw.close();
            
            
            FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+FileUtil.seed_file);
            out.write(seed.getBytes());
            out.flush();
            out.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	
	public static void generateFAVLogInfo(String seed, String testID, List<String> logInfo) {
		String rootReport = FileUtil.root_tmp+testID+"/"+"fuzz.log";

        try {
            File tofile = new File(rootReport);

            if (!tofile.getParentFile().exists()) {
                tofile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(tofile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println("FAVLog info: ");
            for(String s :logInfo) {
                pw.println(s);
            }
            pw.println("");

            pw.close();
            
            
            FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+FileUtil.seed_file);
            out.write(seed.getBytes());
            out.flush();
            out.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	
	public static void writePostTestInfo(String testID, int bitmap_size, long exec_s) {
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+traced_size_file);
			out.write(String.valueOf(bitmap_size).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+exec_second_file);
			out.write(FileUtil.parseSecondsToStringTime(exec_s).getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeFaultSeq(String testID, FaultSequence seq) {
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+faultSeqFile);
			out.write(seq.toString().getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeFaultJSONSeq(String testID, FaultSequence seq) {
		Stat.log(FileUtil.class, "writeFaultJSONSeq begin...");
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID+"/"+faultSeqJSONFile);
			Stat.log(FileUtil.class, "writeFaultJSONSeq to " + FileUtil.root_tmp+testID+"/"+faultSeqJSONFile);
			out.write(seq.toJSONString().getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateQueueInfo(String testID, List<QueueEntry> mutates, int handicap) {
		try {
			FileOutputStream out;

			// out = new FileOutputStream(FileUtil.root_queue+testID+"/"+FileUtil.fuzzed_time_file);
			// out.write(String.valueOf(fuzzed_time).getBytes());
			// out.flush();
			// out.close();
			
			out = new FileOutputStream(FileUtil.root_queue+testID+"/"+FileUtil.mutates_size_file);
			out.write(String.valueOf(mutates.size()).getBytes());
			out.flush();
			out.close();
			
			out = new FileOutputStream(FileUtil.root_queue+testID+"/"+FileUtil.handicap_file);
			out.write(String.valueOf(handicap).getBytes());
			out.flush();
			out.close();
			
//			out = new FileOutputStream(FileUtil.root_queue+testID+"/"+FileUtil.mutates_file);
//			for(QueueEntry m:mutates) {
//				
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeMap(String testID, byte[] map, int map_size, int new_bits) {
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID
					+"/"+FileUtil.map_file+"_"+map_size+"("+new_bits+")");
			out.write(map);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void copyDirToPersist(String testID) {
		File src = new File(FileUtil.root_tmp + testID);
		File des = new File(FileUtil.root_persist);
		try {
			FileUtils.copyDirectoryToDirectory(src, des);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void copyDirToBugs(String testID, long execedSeconds) {
		File src = new File(FileUtil.root_tmp+testID);
		String timeInfo = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
		File des = new File(root_bugs + timeInfo);
		try {
			FileUtils.copyDirectoryToDirectory(src, des);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void copyDirToHangs(String testID, long execedSeconds) {
		File src = new File(FileUtil.root_tmp+testID);
		String timeInfo = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
		File des = new File(root_hangs + timeInfo);
		try {
			FileUtils.copyDirectoryToDirectory(src, des);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void copyToTested(String testID, long execedSeconds, String curFaultFileName) {
    	String timeInfo = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
    	File des = new File(root_tested + timeInfo +"/"+testID);
    	
        File faultFile = new File(FileUtil.root_tmp+testID+"/"+curFaultFileName);
        if(faultFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(faultFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		File seedFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.seed_file);
        if(seedFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(seedFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		File faultSeqFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.faultSeqFile);
		if(faultSeqFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(faultSeqFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		File faultSeqJsonFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.faultSeqJSONFile);
		if(faultSeqJsonFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(faultSeqJsonFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		File mapFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.map_file);
		if(mapFile.exists()){
			try {
				FileUtils.copyFileToDirectory(mapFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        // File tmpFile = new File(FileUtil.root_tmp+testID);
        // if(tmpFile.exists() && tmpFile.isDirectory()) {
        // 	for(File f:tmpFile.listFiles()) {
        // 		if(f.getName().startsWith(FileUtil.map_file)) {
        // 			try {
		// 				FileUtils.copyFileToDirectory(f, des);
		// 			} catch (IOException e) {
		// 				// TODO Auto-generated catch block
		// 				e.printStackTrace();
		// 			}
        // 		}
        // 	}
        // }
	}

    public static void removeFromQueue(String testID, Conf conf) {
    	try {
    		File src = new File(FileUtil.root_queue+testID);
			FileUtils.deleteDirectory(src);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void removeFromHang(String testID, Conf conf) {
    	try {
    		File src = new File(FileUtil.root_hangs+testID);
			FileUtils.deleteDirectory(src);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public static void copyToQueue(String testID, String curFaultFileName) {
        File ioTraces = new File(FileUtil.root_tmp+testID+"/"+ioTracesDir);
        
        if(ioTraces.exists()){
        	try {
        		File des = new File(root_queue + testID);
        		
				FileUtils.copyDirectoryToDirectory(ioTraces, des);
				
				File faultSeq = new File(FileUtil.root_tmp+testID+"/"+curFaultFileName);
				if(faultSeq.exists()) {
					FileUtils.copyFileToDirectory(faultSeq, des);
				}
				
				File exec_s = new File(FileUtil.root_tmp+testID+"/"+FileUtil.exec_second_file);
				if(exec_s.exists()) {
					FileUtils.copyFileToDirectory(exec_s, des);
				}
				
				File map_size = new File(FileUtil.root_tmp+testID+"/"+FileUtil.traced_size_file);
				if(map_size.exists()) {
					FileUtils.copyFileToDirectory(map_size, des);
				}
				

		        File seedFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.seed_file);
		        if(seedFile.exists()){
		        	FileUtils.copyFileToDirectory(seedFile, des);
		        }

				File mapFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.map_file);
				if (mapFile.exists()) {
					FileUtils.copyFileToDirectory(mapFile, des);
				}
		        
		        // File tmpFile = new File(FileUtil.root_tmp+testID);
		        // if(tmpFile.exists() && tmpFile.isDirectory()) {
		        // 	for(File f:tmpFile.listFiles()) {
		        // 		if(f.getName().startsWith(FileUtil.map_file)) {
		        // 			try {
				// 				FileUtils.copyFileToDirectory(f, des);
				// 			} catch (IOException e) {
				// 				// TODO Auto-generated catch block
				// 				e.printStackTrace();
				// 			}
		        // 		}
		        // 	}
		        // }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public static void copyToUntriggered(String testID, Conf conf) {
		File des = new File(root_non_triggered + testID);
    	
        File faultFile = new File(FileUtil.root_tmp+testID+"/"+conf.CUR_FAULT_FILE.getName());
        File seedFile = new File(FileUtil.root_tmp+testID+"/"+FileUtil.seed_file);
        
        if(faultFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(faultFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        if(seedFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(seedFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

	
	
	public static void recordSkippedTests(String testID, List<QueueEntry> mutates, Conf conf) {
		int count = 1;
		for(QueueEntry m:mutates) {
			File f = new File(FileUtil.root_skipped+testID+"/mutation"+count+"/"+conf.CUR_FAULT_FILE.getName());
			genereteFaultSequenceFile(m.faultSeq,f);
		}
	}
	
	public static void genereteFaultSequenceFile(FaultSequence faultSequence, File tofile) {
		if(faultSequence != null && !faultSequence.isEmpty()) {
			if (!tofile.getParentFile().exists()) {
	            tofile.getParentFile().mkdirs();
	        }

			try {
				FileWriter fw = new FileWriter(tofile);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);

				for(FaultPoint p:faultSequence.seq) {
					pw.write("fault point="+p.toString().hashCode()+"\n");
					pw.write("event="+p.type+"\n");
					pw.write("pos="+p.pos+"\n");
					pw.write("nodeIp="+p.tarNodeIp+"\n");
					pw.write("ioID="+p.ioPt.ioID+"\n");
					pw.write("ioCallStack="+p.ioPt.CALLSTACK+"\n");
					pw.write("path="+p.ioPt.PATH+"\n");
					pw.write("ioAppearIdx="+p.ioPt.appearIdx+"\n");
					pw.write("end"+"\n");
				}
				
				pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static FaultSequence loadcurrentFaultPoint(String cur_crash_path) {
		FaultSequence faultSeq = null;
    	try {
            faultSeq = new FaultSequence();
            faultSeq.seq = new ArrayList<FaultPoint>();
            // faultSeq.curFault = -1;
			

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
            		if(content.trim().equals(FaultType.CRASH.toString())) {
            			p.type = FaultType.CRASH;
            		} else if(content.trim().equals(FaultType.REBOOT.toString())) {
            			p.type = FaultType.REBOOT;
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return faultSeq;
    }
	
	public static void copyFileToDir(String src, String des) {
        File sourceFile = new File(src);
        
        if(sourceFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(sourceFile, new File(des));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public static void delete(String src) {
        File sourceFile = new File(src);
        
        if(sourceFile.exists()){
        	if(sourceFile.isDirectory()) {
        		try {
					FileUtils.deleteDirectory(sourceFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	} else if (sourceFile.isFile()) {
        		sourceFile.delete();
        	}
        }
	}
	
	public static long parseStringTimeToSeconds(String time) {
		long rst = 0;
		
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		
		if(time.indexOf("h")>0) {
			String hourString = time.substring(0, time.indexOf("h"));
			hours = Long.parseLong(hourString.trim());
			time = (time.indexOf("h")+1)>= time.length()?"":time.substring(time.indexOf("h")+1);
		}
		
		if(time.indexOf("m")>0) {
			String minutesString = time.substring(0, time.indexOf("m"));
			minutes = Long.parseLong(minutesString.trim());
			time = (time.indexOf("m")+1)>= time.length()?"":time.substring(time.indexOf("m")+1);
		}
		
		if(time.indexOf("s")>0) {
			String secondString = time.substring(0, time.indexOf("s"));
			seconds = Long.parseLong(secondString.trim());
		}
		
		rst = hours*60*60+minutes*60+seconds;
		return rst;
	}
	public static String parseSecondsToStringTime(long time) {
		String rst = "";
		
		long hours = time/(60*60);
		long minutes = (time%(60*60))/60;
		long seconds = (time%(60*60))%60;
		
		if(hours > 0) {
			rst = rst + hours+"h";
		}
		if(minutes > 0) {
			rst = rst + minutes+"m";
		}
		if(seconds > 0) {
			rst = rst + seconds+"s";
		}
		return rst;
	}

	public static void clearRootPath() {
		File src = new File(FileUtil.root);
		try {
			//create the root folder if it does not exist
			if(!src.exists()) {
				src.mkdirs();
			}
			// clear all the files and folders in the root folder
			FileUtils.cleanDirectory(src);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static QueueEntry retriveReplayQueueEntryFromRSTFolder(String filepath, String faultUnderTest) {
		EntryConstructor fsc = new EntryConstructor();
		List<IOPoint> ioPoints = fsc.constructIOPointList(filepath + "/" + ioTracesDir);
		QueueEntry e = new QueueEntry();
		e.ioSeq = ioPoints;
		FaultSequence faultSeq = loadcurrentFaultPoint(filepath + "/" + faultUnderTest);
		if (faultSeq == null) {
			faultSeq = new FaultSequence();
		}
		e.faultSeq = faultSeq;
	
		// TODO: Need to consider whether the workload information should be stored in
		// the RST folder
		e.workload = Conf.currentWorkload;
	
		return e;
	}
}
