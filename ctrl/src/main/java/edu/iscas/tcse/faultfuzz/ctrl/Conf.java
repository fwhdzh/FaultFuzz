package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultStat;
import edu.iscas.tcse.faultfuzz.ctrl.Stat.LOG_LEVEL_SET;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class Conf {
	public static boolean DEBUG = false;
	public static boolean MANUAL = false;
	
    public File FAV_TRIGGER_CONFIG; //store the path of the configuration file which contains .sh file paths that used to start cluster, run workload, where to inject crashes and .etc.
    public File PRETREATMENT; //store the .sh file to clean and prepare the target system
    public File WORKLOAD; //store the .sh file to run the workload
    public File CHECKER;
    public File CRASH;  //crash a node and do check
    public File RESTART;  //restart a node and do check
    public File FAV_BUG_REPORT;
    public int CONTROLLER_PORT;
    public File CUR_FAULT_FILE;
    public File UPDATE_FAULT;
    public File MONITOR;
	public List<File> FAV_MONITOR_DIRS;
	public String FAULT_CONFIG;
	public List<MaxDownNodes> maxDownGroup;
	public long maxTestMinutes = Long.MAX_VALUE;
	public long hangSeconds = 10;
	public static int MAP_SIZE = 10000;
	public long similarBehaviorWindow = 1000;//timestamp value millisecond
	public int AFL_PORT;
	public int MAX_FAULTS = Integer.MAX_VALUE;
    
    
    public boolean RECOVERY_MODE = false;
    // public String RECOVERY_ROOT_PATH = "/data/fengwenhan/data/crashfuzz_fwh";
    // public String RECOVERY_FUZZINFO_PATH = "/data/fengwenhan/data/crashfuzz_fwh/FuzzInfo.txt";
    // public String RECOVERY_CANDIDATEQUEUE_PATH = "/data/fengwenhan/data/crashfuzz_fwh/CandidateQueue.txt";
    // public String RECOVERY_TESTEDFAULTID_PATH = "/data/fengwenhan/data/crashfuzz_fwh/TestedFaultId.txt";
    // public String RECOVERY_VIRGINBITS_PATH = "/data/fengwenhan/data/crashfuzz_fwh/VirginBits.txt";
    public String RECOVERY_FUZZINFO_PATH;
    public String RECOVERY_CANDIDATEQUEUE_PATH;
    public String RECOVERY_TESTEDFAULTID_PATH;
    public String RECOVERY_VIRGINBITS_PATH;

    /*
     * Some functions have not been tested fully.
     * So, these configurations will not be provided to users for the time being
     */
    public boolean REPLAY_MODE = false;
    public String REPLAY_QUEUEENTRY_PATH = "/data/fengwenhan/data/crashfuzz_fwh/replay/QueueEntry.txt";
    // public String REPLAY_TRACE_PATH = "/data/fengwenhan/data/crashfuzz_backup_6_full_workload/queue/6_2f";
    public String REPLAY_TRACE_PATH;
    public long REPLAY_HANG_TIME = 40;
    public String REPLAY_ACTUAL_FPB_LIST_PATH = "/data/fengwenhan/data/crashfuzz_fwh/actualFPBList.txt";
    public int FAULT_SEQUENCE_BATCH_SIZE = 1;

    public int DETERMINE_WAIT_TIME = 10000;
    public File WRITE_FAV_ENV = new File("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/write-fav-env.sh");
    public File COPY_ENV_TO_CLUSTER = new File("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/copy-env-to-cluster.sh");

    public File COPY_LOGS_TO_CONTROLLER;
    public String CLUSTER_LOGS_IN_CONTROLLER_DIR;

    public File NETWORK_DISCONNECT;  // disconnect the network from sourceNode to targetNode
    public File NETWORK_CONNECT;  // connect the network from sourceNode to targetNode

    public static List<FaultStat> s = Arrays.asList(FaultStat.values());
    // public static List<FaultStat> s = Arrays.asList(FaultStat.CRASH, FaultStat.REBOOT);

    public static LOG_LEVEL_SET LOG_LEVEL = LOG_LEVEL_SET.DEBUG;

    public enum EVALUATE_TARGET_SET {
        FaultFuzzer,
        CrashFuzzerMinus,
        BruteForce
    }

    public EVALUATE_TARGET_SET EVALUATE_TARGET;

	
	public Conf(File configFile) {
		FAV_TRIGGER_CONFIG = configFile;
	}

    public void loadConfiguration() throws IOException {
    	InputStream in = new BufferedInputStream(new FileInputStream(FAV_TRIGGER_CONFIG));
        Properties p = new Properties();
        p.load(in);
        
        String workdir = System.getProperty("user.dir").trim()+"/";

        String workload = p.getProperty(ConfOption.WORKLOAD.toString());
        if(workload != null) {
            if(!workload.startsWith("/")) {
            	workload = workdir + workload;
            }
        	File f = new File(workload);
            if(f.exists()) {
            	WORKLOAD = f;
            } else {
            	throw new IOException();
            }
        }

        String curFaultFile = p.getProperty(ConfOption.CUR_FAULT_FILE.toString());
        if(curFaultFile != null) {
            if(!curFaultFile.startsWith("/")) {
            	curFaultFile = workdir + curFaultFile;
            }
        	File f = new File(curFaultFile);
        	CUR_FAULT_FILE = f;
        }
        
        String mapSize = p.getProperty(ConfOption.MAP_SIZE.toString());
        if(mapSize != null) {
        	MAP_SIZE = Integer.parseInt(mapSize);
        }
        
        String aflPort = p.getProperty(ConfOption.AFL_PORT.toString());
        if(aflPort != null) {
        	AFL_PORT = Integer.parseInt(aflPort);
        }
        
        String window = p.getProperty(ConfOption.WINDOW_SIZE.toString());
        if(window != null) {
        	similarBehaviorWindow = Long.parseLong(window);
        }
        
        String maxFaults = p.getProperty(ConfOption.MAX_FAULTS.toString());
        if(maxFaults != null) {
        	MAX_FAULTS = Integer.parseInt(maxFaults);
        }
        
        String testTime = p.getProperty(ConfOption.TEST_TIME.toString());
        if(testTime != null) {
        	maxTestMinutes = FileUtil.parseStringTimeToSeconds(testTime)/60;
        }
        
        String hangTMOut = p.getProperty(ConfOption.HANG_TMOUT.toString());
        if(hangTMOut != null) {
        	hangSeconds = FileUtil.parseStringTimeToSeconds(hangTMOut);
        }

        String faultConfig = p.getProperty(ConfOption.FAULT_CSTR.toString());
    	maxDownGroup = new ArrayList<MaxDownNodes>();
        if(faultConfig != null) {
        	FAULT_CONFIG = faultConfig; //1:{ip1,ip2,ip3};2:{ip4,ip5}
        	String[] groups = FAULT_CONFIG.trim().split(";");
        	for(String group:groups) {
        		String[] secs = group.trim().split(":");
        		int maxDown = Integer.parseInt(secs[0]);
        		String[] ips = secs[1].trim().substring(1, secs[1].trim().length()-1).split(",");
        		Set<String> ipSet = new HashSet<String>();
        		for(String ip:ips) {
        			ipSet.add(ip.trim());
        		}
        		assert(maxDown<ipSet.size());
        		MaxDownNodes downGroup = new MaxDownNodes();
        		downGroup.maxDown = maxDown;
        		downGroup.aliveGroup = ipSet;
        		
        		downGroup.deadGroup = new HashSet<String>();
        		maxDownGroup.add(downGroup);
        	}
        }

        String pretreatment = p.getProperty(ConfOption.PRETREATMENT.toString());
        if(pretreatment != null) {
            if(!pretreatment.startsWith("/")) {
            	pretreatment = workdir + pretreatment;
            }
        	File f = new File(pretreatment);
            if(f.exists()) {
            	PRETREATMENT = f;
            }
        }

        String checker = p.getProperty(ConfOption.CHECKER.toString());
        if(checker != null) {
            if(!checker.startsWith("/")) {
            	checker = workdir + checker;
            }
        	File f = new File(checker);
            if(f.exists()) {
            	CHECKER = f;
            }
        }

        String monitor = p.getProperty(ConfOption.MONITOR.toString());
        if(monitor != null) {
            if(!monitor.startsWith("/")) {
            	monitor = workdir + monitor;
            }
            File f = new File(monitor);
            if(f.exists()) {
                MONITOR = f;
            }
        }
        
        String root = p.getProperty(ConfOption.ROOT_DIR.toString());
        if(root != null) {
            if(!root.startsWith("/")) {
            	root = workdir + root;
            }
        	if(root.trim().endsWith("/")) {
                FileUtil.root = root.trim();
        	} else {
        		FileUtil.root = root.trim()+"/";
        	}
        	FileUtil.init(FileUtil.root);
        }

        String updateCurFault = p.getProperty(ConfOption.UPDATE_FAULT.toString());
        if(updateCurFault != null) {
            if(!updateCurFault.startsWith("/")) {
            	updateCurFault = workdir + updateCurFault;
            }
            File f = new File(updateCurFault);
            if(f.exists()) {
                UPDATE_FAULT = f;
            }
        }

        String checkCrash = p.getProperty(ConfOption.CRASH.toString());
        if(checkCrash != null) {
            if(!checkCrash.startsWith("/")) {
            	checkCrash = workdir + checkCrash;
            }
        	File f = new File(checkCrash);
            if(f.exists()) {
            	CRASH = f;
            } else {
            	throw new IOException();
            }
        }

        String checkRestart = p.getProperty(ConfOption.RESTART.toString());
        if(checkRestart != null) {
            if(!checkRestart.startsWith("/")) {
            	checkRestart = workdir + checkRestart;
            }
        	File f = new File(checkRestart);
            if(f.exists()) {
            	RESTART = f;
            }
        }

        String report = p.getProperty(ConfOption.BUG_REPORT.toString());
        if(report != null) {
            if(!report.startsWith("/")) {
            	report = workdir + report;
            }
        	File f = new File(report);
        	if (!f.getParentFile().exists()) {
	            f.getParentFile().mkdirs();
	        }
        	FAV_BUG_REPORT = f;
        } else {
        	File f = new File("./report");
        	if (!f.getParentFile().exists()) {
	            f.getParentFile().mkdirs();
	        }
        	FAV_BUG_REPORT = f;
        }

        String monitorLine = p.getProperty(ConfOption.MONITOR_DIRS.toString());
        if(monitorLine != null) {
            String[] monitorDirs = monitorLine.trim().split(":");
        	for(String dir:monitorDirs) {
        		File f = new File(dir);
                if(f.exists()) {
                	if(FAV_MONITOR_DIRS == null) {
                		FAV_MONITOR_DIRS = new ArrayList<File>();
                	}
                	FAV_MONITOR_DIRS.add(f);
                }
        	}
        }

        String recoveryMode = p.getProperty(ConfOption.RECOVERY_MODE.toString());
        if(recoveryMode != null) {
        	RECOVERY_MODE = Boolean.parseBoolean(recoveryMode);
        }

        String recoveryDir = p.getProperty(ConfOption.RECOVERY_DIR.toString());
        if(recoveryDir != null) {
            if(!recoveryDir.startsWith("/")) {
            	recoveryDir = workdir + recoveryDir;
            }
        	RECOVERY_FUZZINFO_PATH = recoveryDir + "/FuzzInfo.txt";
            RECOVERY_CANDIDATEQUEUE_PATH = recoveryDir + "/CandidateQueue.txt";
            RECOVERY_TESTEDFAULTID_PATH = recoveryDir + "/TestedFaultId.txt";
            RECOVERY_VIRGINBITS_PATH = recoveryDir + "/VirginBits.txt";
        }

        String replayMode = p.getProperty(ConfOption.REPLAY_MODE.toString());
        if(replayMode != null) {
        	REPLAY_MODE = Boolean.parseBoolean(replayMode);
        }

        String replayTracePath = p.getProperty(ConfOption.REPLAY_TRACE_PATH.toString());
        if(replayTracePath != null) {
        	REPLAY_TRACE_PATH = replayTracePath;
        }

        String determineWaitTime = p.getProperty(ConfOption.DETERMINE_WAIT_TIME.toString());
        if(determineWaitTime != null) {
        	DETERMINE_WAIT_TIME = Integer.parseInt(determineWaitTime);
        }

        String writeFavEnv = p.getProperty(ConfOption.WRITE_FAV_ENV.toString());
        if (writeFavEnv != null) {
            if (!writeFavEnv.startsWith("/")) {
                writeFavEnv = workdir + writeFavEnv;
            }
            File f = new File(writeFavEnv);
            if (f.exists()) {
                WRITE_FAV_ENV = f;
            }
        }

        String copyEnvToCluster = p.getProperty(ConfOption.COPY_ENV_TO_CLUSTER.toString());
        if (copyEnvToCluster != null) {
            if (!copyEnvToCluster.startsWith("/")) {
                copyEnvToCluster = workdir + copyEnvToCluster;
            }
            File f = new File(copyEnvToCluster);
            if (f.exists()) {
                COPY_ENV_TO_CLUSTER = f;
            }
        }

        String copyLogsToController = p.getProperty(ConfOption.COPY_LOGS_TO_CONTROLLER.toString());
        if (copyLogsToController != null) {
            if (!copyLogsToController.startsWith("/")) {
                copyLogsToController = workdir + copyLogsToController;
            }
            File f = new File(copyLogsToController);
            if (f.exists()) {
                COPY_LOGS_TO_CONTROLLER = f;
            }
        }

        String clusterlogsInControllerDir = p.getProperty(ConfOption.CLUSTER_LOGS_IN_CONTROLLER_DIR.toString());
        if(clusterlogsInControllerDir != null) {
        	CLUSTER_LOGS_IN_CONTROLLER_DIR = clusterlogsInControllerDir;
        }

        String networkDisconnect = p.getProperty(ConfOption.NETWORK_DISCONNECTION.toString());
        if (networkDisconnect != null) {
            if (!networkDisconnect.startsWith("/")) {
                networkDisconnect = workdir + networkDisconnect;
            }
            File f = new File(networkDisconnect);
            if (f.exists()) {
                NETWORK_DISCONNECT = f;
            }
        }

        String networkConnect = p.getProperty(ConfOption.NETWORK_CONNECTION.toString());
        if (networkConnect != null) {
            if (!networkConnect.startsWith("/")) {
                networkConnect = workdir + networkConnect;
            }
            File f = new File(networkConnect);
            if (f.exists()) {
                NETWORK_CONNECT = f;
            }
        }

        String evaluateTarget = p.getProperty(ConfOption.EVALUATE_TARGET.toString());
        // System.out.println(evaluateTarget);
        if(evaluateTarget != null) {
        	EVALUATE_TARGET = EVALUATE_TARGET_SET.valueOf(evaluateTarget);
        } else {
            EVALUATE_TARGET = EVALUATE_TARGET_SET.FaultFuzzer;
            // System.out.println(EVALUATE_TARGET);
        }

        String loglevel = p.getProperty(ConfOption.LOG_LEVEL.toString());
        // System.out.println(evaluateTarget);
        if(loglevel != null) {
        	LOG_LEVEL = LOG_LEVEL_SET.valueOf(loglevel);
        } else {
            LOG_LEVEL = LOG_LEVEL_SET.INFO;
            // System.out.println(EVALUATE_TARGET);
        }

        

        if(RESTART == null || CRASH == null || WORKLOAD == null || CUR_FAULT_FILE == null || PRETREATMENT == null) {
        	throw new IOException();
        }

        System.out.println("=========================FaultFuzz Configuration=========================");
        System.out.println("Controller port: "+CONTROLLER_PORT);
        System.out.println("Configuration file: "+FAV_TRIGGER_CONFIG.getAbsolutePath());
        System.out.println("Root report path: "+FileUtil.root);
        System.out.println("Current crash point file: "+CUR_FAULT_FILE.getAbsolutePath());
        System.out.println("Update current crash point script: "+UPDATE_FAULT.getAbsolutePath());
        System.out.println("Prepare cluster script: "+(PRETREATMENT==null?"":PRETREATMENT.getAbsolutePath()));
        System.out.println("Workload script: "+WORKLOAD.getAbsolutePath());
        System.out.println("Checker script: "+CHECKER.getAbsolutePath());
        System.out.println("Crash node script: "+CRASH.getAbsolutePath());
        System.out.println("Restart node script: "+(RESTART==null?"":RESTART.getAbsolutePath()));
        System.out.println("Monitor script: "+(MONITOR==null?"":MONITOR.getAbsolutePath()));
        System.out.println("Max test time: "+FileUtil.parseSecondsToStringTime(this.maxTestMinutes*60));
        System.out.println("Hang timeout: "+FileUtil.parseSecondsToStringTime(this.hangSeconds));
//        System.out.println("Similar points window: "+this.similarBehaviorWindow+"ms");
        System.out.println("Max fault number: "+this.MAX_FAULTS);
        System.out.println("Fault constraints: ");
        for(MaxDownNodes group:maxDownGroup) {
        	System.out.println("For nodes "+group.aliveGroup+", allowed max down nodes at same time is:"+group.maxDown);
        }

        // System.out.println("Recovery mode: " + RECOVERY_MODE);
        // System.out.println("Recovery fuzzinfo path: " + (RECOVERY_MODE ? RECOVERY_FUZZINFO_PATH : ""));
        // System.out.println("Recovery candidate queue path: " + (RECOVERY_MODE ? RECOVERY_CANDIDATEQUEUE_PATH : ""));
        // System.out.println("Recovery tested fault id path: " + (RECOVERY_MODE ? RECOVERY_TESTEDFAULTID_PATH : ""));
        // System.out.println("Recovery virgin bits path: " + (RECOVERY_MODE ? RECOVERY_VIRGINBITS_PATH : ""));

        // System.out.println("Replay mode: " + REPLAY_MODE);
        // System.out.println("Replay trace path: " + REPLAY_TRACE_PATH);

        System.out.println("networkDisConnect script: " + NETWORK_DISCONNECT.getAbsolutePath());
        System.out.println("networkConnect script: " + NETWORK_CONNECT.getAbsolutePath());
        System.out.println("evaluate target: " + EVALUATE_TARGET.toString());
        System.out.println("log level: " + LOG_LEVEL.toString());

        System.out.println("=======================================================================");
    }
}
