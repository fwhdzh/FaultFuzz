package edu.iscas.tcse.faultfuzz.ctrl.control;

import java.io.File;
import java.util.ArrayList;

import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class AbstractController {
	public static class AbortFaultException extends Exception {
		public AbortFaultException(String errorMessage) {
	        super(errorMessage);
	    }
	}

	public Cluster cluster;
	public boolean serverThreadRunning;
	public int CONTROLLER_PORT = 8888;
	public ArrayList<String> rst;
    // public Conf favconfig;
	public final int maxClients = 300;

	public File curFaultFile = null;

    // public AbstractController(Cluster cluster, int port, Conf favconfig) {
    // 	this.cluster = cluster;
    // 	this.running = false;
    // 	this.CONTROLLER_PORT = port;
    // 	// this.favconfig = favconfig;
    // 	this.rst = new ArrayList<String>();

	// 	this.curFaultFile = favconfig.CUR_FAULT_FILE;
    // }

	public AbstractController(Cluster cluster, int port, File curFaultFile) {
    	this.cluster = cluster;
    	this.serverThreadRunning = false;
    	this.CONTROLLER_PORT = port;
    	// this.favconfig = favconfig;
    	this.rst = new ArrayList<String>();

		this.curFaultFile = curFaultFile;
    }

    public void updataCurFaultFile(FaultSequence faultSequence) {
		if(faultSequence == null || faultSequence.isEmpty()) {
			// File file = favconfig.CUR_FAULT_FILE;
			// if(file.exists()) {
			//     file.delete();
			// }
			if (curFaultFile.exists()) {
				curFaultFile.delete();
			}
		} else {
			// FileUtil.genereteFaultSequenceFile(faultSequence, favconfig.CUR_FAULT_FILE);
			FileUtil.genereteFaultSequenceFile(faultSequence, curFaultFile);
		}

	}
}
