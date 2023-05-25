package edu.iscas.tcse.faultfuzz.ctrl.control;

import java.io.File;
import java.util.ArrayList;

import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.RunCommand;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

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
			}
		} else {
			FileUtil.genereteFaultSequenceFile(faultSequence, favconfig.CUR_CRASH_FILE);
		}

		if(favconfig.UPDATE_CRASH != null) {
			String path = favconfig.UPDATE_CRASH.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			RunCommand.run(path, workingDir);
		}
	}
}
