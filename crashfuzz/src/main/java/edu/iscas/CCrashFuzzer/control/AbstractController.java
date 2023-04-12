package edu.iscas.CCrashFuzzer.control;

import java.io.File;
import java.util.ArrayList;

import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.RunCommand;
import edu.iscas.CCrashFuzzer.utils.FileUtil;

public class AbstractController {
	public Cluster cluster;
	public boolean running;
	public int CONTROLLER_PORT = 8888;
	public ArrayList<String> rst;
    public Conf favconfig;
	public final int maxClients = 300;

    public AbstractController(Cluster cluster, int port, Conf favconfig) {
    	this.cluster = cluster;
    	this.running = false;
    	this.CONTROLLER_PORT = port;
    	this.favconfig = favconfig;
    	this.rst = new ArrayList<String>();
    }

    public void updataCurCrashPointFile(FaultSequence faultSequence) {
		if(faultSequence == null || faultSequence.isEmpty()) {
			File file = favconfig.CUR_CRASH_FILE;
			if(file.exists()) {
			    file.delete();
				if(favconfig.UPDATE_CRASH != null) {
		            String path = favconfig.UPDATE_CRASH.getAbsolutePath();
		            String workingDir = path.substring(0, path.lastIndexOf("/"));
		            RunCommand.run(path, workingDir);
		        }
			}
		} else {
			FileUtil.genereteFaultSequenceFile(faultSequence, favconfig.CUR_CRASH_FILE);
			
			if(favconfig.UPDATE_CRASH != null) {
                String path = favconfig.UPDATE_CRASH.getAbsolutePath();
                String workingDir = path.substring(0, path.lastIndexOf("/"));
                RunCommand.run(path, workingDir);
            }
		}
	}
}
