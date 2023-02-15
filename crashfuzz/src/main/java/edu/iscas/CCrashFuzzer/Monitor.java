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

import org.apache.commons.io.FileUtils;

import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class Monitor {
	Conf conf;
	
	public Monitor(Conf conf) {
		this.conf = conf;
	}
	
	public String getTmpReportDir(String testID) {
		if(testID.startsWith("init")) {
			return FileUtil.root_tmp+testID+"/";
		} else {
			return FileUtil.root_tmp+testID+"/";
		}
	}
	
	public void collectRunTimeInfo(String tmpRoot) {
		if(conf.MONITOR != null) {
            String path = conf.MONITOR.getAbsolutePath();
            String workingDir = path.substring(0, path.lastIndexOf("/"));
            File tofile = new File(tmpRoot);
            if (!tofile.getParentFile().exists()) {
                tofile.getParentFile().mkdirs();
            }
            tofile.mkdir();
            RunCommand.run(path+" "+tmpRoot, workingDir);
            //return RunCommand.run(path);
        }
	}
	
	public void copyCurCrash(String src, String rootpath) {
        File sourceFile = new File(src);
        
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
