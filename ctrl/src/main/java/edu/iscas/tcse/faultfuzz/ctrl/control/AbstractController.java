package edu.iscas.tcse.faultfuzz.ctrl.control;

import java.io.File;
import java.util.ArrayList;

import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class AbstractController {
	public static class AbortFaultException extends Exception {
		public AbortFaultException(String errorMessage) {
	        super(errorMessage);
	    }
	}

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

    public void updataCurFaultFile(FaultSequence faultSequence) {
		if(faultSequence == null || faultSequence.isEmpty()) {
			File file = favconfig.CUR_FAULT_FILE;
			if(file.exists()) {
			    file.delete();
			}
		} else {
			FileUtil.genereteFaultSequenceFile(faultSequence, favconfig.CUR_FAULT_FILE);
		}

	}
}
