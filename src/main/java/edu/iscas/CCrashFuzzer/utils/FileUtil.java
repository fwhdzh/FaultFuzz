package edu.iscas.CCrashFuzzer.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.RunCommand;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;

public class FileUtil {
	public static String fuzzer_id_file = "crashfuzz_proc_id";
	
	public static int newBugFileWindow = 30; //minutes
	public static String root = "crashfuzzer/";
	public static String root_tested = root+"tested/";//create a new file every "newBugFileWindow" miniues.
	public static String root_queue = root+"queue/";
	public static String root_fuzzed = root+"fuzzed/";
	public static String root_skipped = root+"skipped/";
	public static String root_non_triggered = root+"miss/";
	public static String root_bugs = root+"bugs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_hangs = root+"hangs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_tmp = root+"tmp/";
	
	public static String monitorDir = "monitor";
	public static String ioTracesDir = "fav-rst";
	public static String coverageDir = "cov";

	public static String seed_file = "SEED";
	
	public static String fuzzed_time_file = "FUZZED_TIME";
	public static String mutates_size_file = "MUTATES_SIZE";
	public static String handicap_file = "HANDICAP";
	public static String mutates_file = "MUTATES";
	
	public static String neighbor_new_covs_file = "ADJACENT_NEW_COVS";
	
	public static String exec_second_file = "EXEC_TIME";
	public static String traced_size_file = "TRACE_SIZE";
	
	public static String total_execution_file = "TOTAL_EXEC_NUM";
	public static String total_map_entry_file = "TOTAL_MAP_ENTRY";
	
	public static String map_file = "MAP";
	public static String virgin_map_file = "VIRGIN_MAP";
	public static String virgin_map_size_file = "VIRGIN_MAP_SIZE";
	
	public static String report_file = "TEST_REPORT";
	
	public static String total_tested_time = "TESTED_TIME";
	
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
	}
	
	public static void generateFAVLogInfo(String seed, String testID, ArrayList<String> logInfo, FaultSequence seq) {
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
	
	public static void generateFAVLogInfo(String seed, String testID, ArrayList<String> logInfo) {
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
	
	public static void updateQueueInfo(String testID, List<QueueEntry> mutates, int fuzzed_time, int handicap) {
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_queue+testID+"/"+FileUtil.fuzzed_time_file);
			out.write(String.valueOf(fuzzed_time).getBytes());
			out.flush();
			out.close();
			
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
	public static void writeNeighborNewCovs(String testID, int new_covs) {
		try {
			FileOutputStream out = new FileOutputStream(FileUtil.root_tmp+testID
					+"/"+FileUtil.neighbor_new_covs_file+"_"+new_covs);
			out.write(String.valueOf(new_covs).getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void copyDirToBugs(String testID, long execedSeconds) {
		File src = new File(FileUtil.root_tmp+testID);
		String suffix = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
		File des = new File(root_bugs + suffix);
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

    public static void copyToTested(String testID, long execedSeconds, Conf conf) {
    	String timeInfo = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
    	File des = new File(root_tested + timeInfo +"/"+testID);
    	
        File faultFile = new File(FileUtil.root_tmp+testID+"/"+conf.CUR_CRASH_FILE.getName());
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
        
        File tmpFile = new File(FileUtil.root_tmp+testID);
        if(tmpFile.exists() && tmpFile.isDirectory()) {
        	for(File f:tmpFile.listFiles()) {
        		if(f.getName().startsWith(FileUtil.map_file)) {
        			try {
						FileUtils.copyFileToDirectory(f, des);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		} else if (f.getName().startsWith(FileUtil.neighbor_new_covs_file)) {
        			try {
						FileUtils.copyFileToDirectory(f, des);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        	}
        }
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
	public static void copyToQueue(String testID, Conf conf) {
        File ioTraces = new File(FileUtil.root_tmp+testID+"/"+ioTracesDir);
        
        if(ioTraces.exists()){
        	try {
        		File des = new File(root_queue + testID);
        		
				FileUtils.copyDirectoryToDirectory(ioTraces, des);
				
				File faultSeq = new File(FileUtil.root_tmp+testID+"/"+conf.CUR_CRASH_FILE.getName());
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
		        
		        File tmpFile = new File(FileUtil.root_tmp+testID);
		        if(tmpFile.exists() && tmpFile.isDirectory()) {
		        	for(File f:tmpFile.listFiles()) {
		        		if(f.getName().startsWith(FileUtil.map_file)) {
		        			try {
								FileUtils.copyFileToDirectory(f, des);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        		} else if (f.getName().startsWith(FileUtil.neighbor_new_covs_file)) {
		        			try {
								FileUtils.copyFileToDirectory(f, des);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        		}
		        	}
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public static void copyToFuzzed(String fname, long execedSeconds) {
    	String timeInfo = newBugFileWindow +"m-"+execedSeconds/(60*newBugFileWindow);
		File des = new File(root_fuzzed+ timeInfo);
    	
        File sourceFile = new File(FileUtil.root_queue + fname);
        
        if(sourceFile.exists()){
        	try {
				FileUtils.copyDirectoryToDirectory(sourceFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public static void copyToUntriggered(String testID, Conf conf) {
		File des = new File(root_non_triggered + testID);
    	
        File faultFile = new File(FileUtil.root_tmp+testID+"/"+conf.CUR_CRASH_FILE.getName());
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
			File f = new File(FileUtil.root_skipped+testID+"/mutation"+count+"/"+conf.CUR_CRASH_FILE.getName());
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
					pw.write("event="+p.stat+"\n");
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
}
