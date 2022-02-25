package edu.iscas.CCrashFuzzer.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import edu.iscas.CCrashFuzzer.FaultSequence;

public class FileUtil {
	public static int newBugFileWindow = 30; //minutes
	public static String root = "crashfuzzer/";
	public static String root_tested = "crashfuzzer/tested/";//create a new file every "newBugFileWindow" miniues.
	public static String root_queue = "crashfuzzer/queue/";
	public static String root_bugs = "crashfuzzer/bugs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_hangs = "crashfuzzer/hangs/";//create a new file every "newBugFileWindow" miniues.
	public static String root_tmp = "crashfuzzer/tmp/";
	
	public static String monitorName = "monitor";
	public static String ioTracesName = "fav-rst";
	public static String coverageName = "cov";
	
	public static void generateFAVLogInfo(String rootDir, ArrayList<String> logInfo, FaultSequence seq) {
		String rootReport = rootDir+"fuzz.log";

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

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	
	public static void copyDirToBugs(String tmpRoot, String testID, long execedMinites) {
		File src = new File(tmpRoot);
		String suffix = newBugFileWindow +"m-"+execedMinites/newBugFileWindow;
		File des = new File(root_bugs + suffix +"/");
		try {
			FileUtils.copyDirectory(src, des);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void copyDirToHangs(String tmpRoot, String testID, long execedMinites) {
		File src = new File(tmpRoot);
		String timeInfo = newBugFileWindow +"m-"+execedMinites/newBugFileWindow;
		File des = new File(root_hangs + timeInfo +"/");
		try {
			FileUtils.copyDirectory(src, des);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void copyToTested(String src, String testID, long execedMinites) {
    	String timeInfo = newBugFileWindow +"m-"+execedMinites/newBugFileWindow;
    	File des = new File(root_tested + timeInfo +"/"+testID);
    	
        File sourceFile = new File(src);
        
        if(sourceFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(sourceFile, des);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

	public static void copyToQueue(String src, String testID) {
		File des = new File(root_queue + testID);
    	
        File sourceFile = new File(src);
        
        if(sourceFile.exists()){
        	try {
				FileUtils.copyFileToDirectory(sourceFile, des);
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
}
