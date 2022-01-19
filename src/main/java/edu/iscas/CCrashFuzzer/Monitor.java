package edu.iscas.CCrashFuzzer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Monitor {
	Conf conf;
	String root = "crashfuzzer/output/";
	public Monitor(Conf conf) {
		this.conf = conf;
	}
	
	public String getRootReport(String testID) {
		if(testID.equals("init")) {
			return root+"init_case/";
		} else {
			return root+testID+"/";
		}
	}
	
	//clear tmp folder
	// put following info under tmp folder
	//first collect coverage info:
	// node1: basic block IDs, node2: basic block IDs
	// use these ids to build a bit map
	//second collect I/O points
	// like deminer + io point ID + appearIdx
	// build a fault sequence
	public void collectRunTimeRst(String testID, ArrayList<String> logInfo, FaultSequence seq) {
	    String rootReport = getRootReport(testID);
        //generateCrashNodeInfo(crashIdx, point, rootReport+"/"+"pointInfo");
        generateFAVLogInfo(testID, logInfo, seq);
        copyCurCrash(rootReport);
        if(conf.MONITOR != null) {
            String path = conf.MONITOR.getAbsolutePath();
            String workingDir = path.substring(0, path.lastIndexOf("/"));
            File tofile = new File(rootReport);
//            if (!tofile.getParentFile().exists()) {
//                tofile.getParentFile().mkdirs();
//            }
            tofile.mkdir();
            RunCommand.run(path+" "+rootReport, workingDir);
            //return RunCommand.run(path);
        }
	}
	
	public void generateFAVLogInfo(String testID, ArrayList<String> logInfo, FaultSequence seq) {
		String rootReport = getRootReport(testID)+"/fuzz.log";

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
	
	public void copyCurCrash(String rootpath) {
        File sourceFile = conf.CUR_CRASH_FILE;

        if(sourceFile.exists()){
            try {
                String movePath = rootpath+"/"+sourceFile.getName();
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(movePath));

                byte[] b = new byte[1024];
                int temp = 0;
                while((temp = in.read(b)) != -1){
                    out.write(b,0,temp);
                }
                out.close();
                in.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
	}
}
