package edu.iscas.tcse.faultfuzz.ctrl.replay;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;

public class ReplayConf {

    public File currentWorkload;
    public File CHECKER;
    public File MONITOR;
    public File PRETREATMENT; //store the .sh file to clean and prepare the target system

    public File CRASH;  //crash a node and do check
    public File REBOOT;  //reboot a node and do check
    public File NETWORK_DISCONNECTION;  // disconnect the network from sourceNode to targetNode
    public File NETWORK_RECONNECTION;  // connect the network from sourceNode to targetNode

    public List<FaultType> faultTypeList = Arrays.asList(FaultType.values());

    public List<MaxDownNodes> maxDownGroup;
    public int AFL_PORT;

    public int CONTROLLER_PORT = -1;
    public long hangSeconds = 10;
    public int DETERMINE_WAIT_TIME = 10000;


    public String DataDir;
    public String reportPath;

    public String REPLAY_TRACE_PATH;
    // public String REPLAY_TRACE_PATH = "/data/fengwenhan/data/crashfuzz_backup_6_full_workload/queue/6_2f";
    

    // Not use these fields yet.
    public String REPLAY_QUEUEENTRY_PATH = "/data/fengwenhan/data/crashfuzz_fwh/replay/QueueEntry.txt";

    public ReplayConf(File currentWorkload, File cHECKER, File mONITOR, File pRETREATMENT, File cRASH, File rEBOOT,
            File nETWORK_DISCONNECTION, File nETWORK_RECONNECTION, List<FaultType> faultTypeList,
            List<MaxDownNodes> maxDownGroup, int aFL_PORT, int cONTROLLER_PORT, long hangSeconds,
            int dETERMINE_WAIT_TIME, String dataDir, String reportPath, String rEPLAY_TRACE_PATH) {
        this.currentWorkload = currentWorkload;
        this.CHECKER = cHECKER;
        this.MONITOR = mONITOR;
        this.PRETREATMENT = pRETREATMENT;
        this.CRASH = cRASH;
        this.REBOOT = rEBOOT;
        this.NETWORK_DISCONNECTION = nETWORK_DISCONNECTION;
        this.NETWORK_RECONNECTION = nETWORK_RECONNECTION;
        this.faultTypeList = faultTypeList;
        this.maxDownGroup = maxDownGroup;
        this.AFL_PORT = aFL_PORT;
        this.CONTROLLER_PORT = cONTROLLER_PORT;
        this.hangSeconds = hangSeconds;
        this.DETERMINE_WAIT_TIME = dETERMINE_WAIT_TIME;
        this.DataDir = dataDir;
        this.REPLAY_TRACE_PATH = rEPLAY_TRACE_PATH;
    }


    public ReplayConf(Conf conf, String tracePath) {
        this.currentWorkload = Conf.currentWorkload;
        this.CHECKER = conf.CHECKER;
        this.MONITOR = conf.MONITOR;
        this.PRETREATMENT = conf.PRETREATMENT;
        this.CRASH = conf.CRASH;
        this.REBOOT = conf.REBOOT;
        this.NETWORK_DISCONNECTION = conf.NETWORK_DISCONNECTION;
        this.NETWORK_RECONNECTION = conf.NETWORK_RECONNECTION;
        this.faultTypeList = Conf.faultTypeList;
        this.maxDownGroup = conf.maxDownGroup;
        this.AFL_PORT = conf.AFL_PORT;
        this.CONTROLLER_PORT = conf.CONTROLLER_PORT;
        this.hangSeconds = conf.hangSeconds;
        this.DETERMINE_WAIT_TIME = conf.DETERMINE_WAIT_TIME;

        // this.DataDir = conf.ROOTDIR;

        this.REPLAY_TRACE_PATH = tracePath;   
    }

    public ReplayConf(Conf conf, String tracePath, String reportPath) {
        this(conf, tracePath);
        this.reportPath = reportPath;
    }
}
