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

import edu.iscas.tcse.faultfuzz.ctrl.Stat.LOG_LEVEL_SET;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class Conf {
	public static boolean DEBUG = false;
	public static boolean MANUAL = false;
	
    public File FAV_TRIGGER_CONFIG; //store the path of the configuration file which contains .sh file paths that used to start cluster, run workload, where to inject crashes and .etc.
    
    public File WORKLOAD; //store the .sh file to run the workload

    public static List<File> WORKLOADLIST;
    public static File currentWorkload;

    public File CHECKER;
    public File MONITOR;
    public File PRETREATMENT; //store the .sh file to clean and prepare the target system

    /**
     * The fault types that will be injected into the system. 
     * Support CRASH, REBOOT, NETWORK_DISCONNECTION, NETWORK_RECONNECTION
     */
    public static List<FaultType> faultTypeList = Arrays.asList(FaultType.values());
    // public static List<FaultStat> faultType = Arrays.asList(FaultStat.CRASH, FaultStat.REBOOT);

    public File CRASH;  //crash a node and do check
    public File REBOOT;  //reboot a node and do check
    public File NETWORK_DISCONNECTION;  // disconnect the network from sourceNode to targetNode
    public File NETWORK_RECONNECTION;  // connect the network from sourceNode to targetNode

    
    public File CUR_FAULT_FILE;
    
	// public String FAULT_CLUSTER;
	public List<MaxDownNodes> maxDownGroup;
	public long maxTestMinutes = Long.MAX_VALUE;
	public long hangSeconds = 10;
	public static int MAP_SIZE = 10000;
	public long similarBehaviorWindow = 1000;//timestamp value millisecond
	public int AFL_PORT;
	public int MAX_FAULTS = Integer.MAX_VALUE;

    public int CONTROLLER_PORT = -1;

    public String ROOTDIR;
    
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

    public int FAULT_SEQUENCE_BATCH_SIZE = 1;

    public int DETERMINE_WAIT_TIME = 10000;
    public File WRITE_FAV_ENV = new File("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/write-fav-env.sh");
    public File COPY_ENV_TO_CLUSTER = new File("/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/copy-env-to-cluster.sh");

    public File COPY_LOGS_TO_CONTROLLER;
    public String CLUSTER_LOGS_IN_CONTROLLER_DIR;

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

    private File handlePath(String path) {
        String workdir = System.getProperty("user.dir").trim()+"/";
        if(path != null) {
            if(!path.startsWith("/")) {
                path = workdir + path;
            }
            File f = new File(path);
            return f;
        }
        return null;
    }

    private File handlePathAndThrowIfNotExist(String path) {
        String workdir = System.getProperty("user.dir").trim()+"/";
        if(path != null) {
            if(!path.startsWith("/")) {
                path = workdir + path;
            }
            File f = new File(path);
            if(f.exists()) {
                return f;
            } else {
                throw new RuntimeException("File not exist: " + path);
            }
        }
        return null;
    }

    public List<MaxDownNodes> parseMaxDownGroup(String faultConfig) {
        List<MaxDownNodes> result = new ArrayList<MaxDownNodes>();
        String[] groups = faultConfig.trim().split(";"); // 1:{ip1,ip2,ip3};2:{ip4,ip5}
        for (String group : groups) {
            String[] secs = group.trim().split(":");
            int maxDown = Integer.parseInt(secs[0]);
            String[] ips = secs[1].trim().substring(1, secs[1].trim().length() - 1).split(",");
            Set<String> ipSet = new HashSet<String>();
            for (String ip : ips) {
                ipSet.add(ip.trim());
            }
            assert (maxDown < ipSet.size());
            MaxDownNodes downGroup = new MaxDownNodes();
            downGroup.maxDown = maxDown;
            downGroup.aliveGroup = ipSet;

            downGroup.deadGroup = new HashSet<String>();
            result.add(downGroup);
        }
        return result;
    }

    public void loadConfiguration() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(FAV_TRIGGER_CONFIG));
        Properties p = new Properties();
        p.load(in);
        
        String workdir = System.getProperty("user.dir").trim()+"/";

        String workload = p.getProperty(ConfOption.WORKLOAD.toString());
        if(workload != null) {
            if (workload.startsWith("{") && workload.endsWith("}")) {
                workload = workload.substring(1, workload.length() - 1);
                workload = workload.trim();
                String[] workloadArray = workload.split(",");
                List<File> workloadList = new ArrayList<>();
                for (String workloadItem : workloadArray) {
                    workloadList.add(handlePathAndThrowIfNotExist(workloadItem));
                }
                WORKLOADLIST = workloadList;
                currentWorkload = WORKLOADLIST.get(0);
            } else {
                WORKLOAD = handlePathAndThrowIfNotExist(workload);
                WORKLOADLIST = new ArrayList<>();
                WORKLOADLIST.add(WORKLOAD);
                currentWorkload = WORKLOADLIST.get(0);
            }
        }

        String faultType = p.getProperty(ConfOption.FAULT_TYPE.toString());
        if (faultType != null) {
            faultType = faultType.substring(1, faultType.length() - 1);
            String[] faultTypeArray = faultType.split(",");
            List<FaultType> faultTypeList = new ArrayList<>();
            for (String faultTypeItem : faultTypeArray) {
                faultTypeList.add(FaultType.valueOf(faultTypeItem));
            }
            Conf.faultTypeList = faultTypeList;
        }

        String curFaultFile = p.getProperty(ConfOption.CUR_FAULT_FILE.toString());
        if(curFaultFile != null) {
            File f = handlePath(curFaultFile);
        	CUR_FAULT_FILE = f;
        }

        String controllerPort = p.getProperty(ConfOption.CONTROLLER_PORT.toString());
        if(controllerPort != null) {
        	CONTROLLER_PORT = Integer.parseInt(controllerPort);
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
        
        String hangTimeOut = p.getProperty(ConfOption.HANG_TIMEOUT.toString());
        if(hangTimeOut != null) {
        	hangSeconds = FileUtil.parseStringTimeToSeconds(hangTimeOut);
        }

        String faultConfig = p.getProperty(ConfOption.FAULT_CSTR.toString());
        maxDownGroup = parseMaxDownGroup(faultConfig);

        String pretreatment = p.getProperty(ConfOption.PRETREATMENT.toString());
        if(pretreatment != null) {
            File f = handlePath(pretreatment);
            PRETREATMENT = f;
        }

        String checker = p.getProperty(ConfOption.CHECKER.toString());
        if(checker != null) {
            File f = handlePath(checker);
            CHECKER = f;
        }

        String monitor = p.getProperty(ConfOption.MONITOR.toString());
        if(monitor != null) {
            File f = handlePath(monitor);
            MONITOR = f;
        }
        
        String root = p.getProperty(ConfOption.ROOT_DIR.toString());
        
        if(root != null) {
            if(!root.startsWith("/")) {
            	root = workdir + root;
            }
        	if(root.trim().endsWith("/")) {
                ROOTDIR = root.trim();
        	} else {
                ROOTDIR = root.trim()+"/";
        	}
        	FileUtil.init(ROOTDIR);
        }

        String checkCrash = p.getProperty(ConfOption.CRASH.toString());
        if(checkCrash != null) {
            File f = handlePath(checkCrash);
            CRASH = f;
        }

        String checkRestart = p.getProperty(ConfOption.REBOOT.toString());
        if(checkRestart != null) {
            File f = handlePath(checkRestart);
            REBOOT = f;
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

        String determineWaitTime = p.getProperty(ConfOption.DETERMINE_WAIT_TIME.toString());
        if(determineWaitTime != null) {
        	DETERMINE_WAIT_TIME = Integer.parseInt(determineWaitTime);
        }

        String writeFavEnv = p.getProperty(ConfOption.WRITE_FAV_ENV.toString());
        if (writeFavEnv != null) {
            File f = handlePath(writeFavEnv);
            WRITE_FAV_ENV = f;
        }

        String copyEnvToCluster = p.getProperty(ConfOption.COPY_ENV_TO_CLUSTER.toString());
        if (copyEnvToCluster != null) {
            File f = handlePath(copyEnvToCluster);
            COPY_ENV_TO_CLUSTER = f;
        }

        String copyLogsToController = p.getProperty(ConfOption.COPY_LOGS_TO_CONTROLLER.toString());
        if (copyLogsToController != null) {
            File f = handlePath(copyLogsToController);
            COPY_LOGS_TO_CONTROLLER = f;
        }

        String clusterlogsInControllerDir = p.getProperty(ConfOption.CLUSTER_LOGS_IN_CONTROLLER_DIR.toString());
        if(clusterlogsInControllerDir != null) {
        	CLUSTER_LOGS_IN_CONTROLLER_DIR = clusterlogsInControllerDir;
        }

        String networkDisconnection = p.getProperty(ConfOption.NETWORK_DISCONNECTION.toString());
        if (networkDisconnection != null) {
            File f = handlePath(networkDisconnection);
            NETWORK_DISCONNECTION = f;
        }

        String networkReconnection = p.getProperty(ConfOption.NETWORK_RECONNECTION.toString());
        if (networkReconnection != null) {
            File f = handlePath(networkReconnection);
            NETWORK_RECONNECTION = f;
        }

        String evaluateTarget = p.getProperty(ConfOption.EVALUATE_TARGET.toString());
        if(evaluateTarget != null) {
        	EVALUATE_TARGET = EVALUATE_TARGET_SET.valueOf(evaluateTarget);
        } else {
            EVALUATE_TARGET = EVALUATE_TARGET_SET.FaultFuzzer;
        }

        String loglevel = p.getProperty(ConfOption.LOG_LEVEL.toString());
        if(loglevel != null) {
        	LOG_LEVEL = LOG_LEVEL_SET.valueOf(loglevel);
        } else {
            LOG_LEVEL = LOG_LEVEL_SET.INFO;
        }
    }

    public void loadConfigurationAndCheckAndPrint() throws IOException {
        loadConfiguration();
        if (!checkLegal()) {
            throw new IOException();
        }
        printConfInformation();
    }

    public boolean checkLegal() throws IOException {
        if (REBOOT == null || CRASH == null || WORKLOADLIST == null
                || WORKLOADLIST.size() == 0 || CUR_FAULT_FILE == null || PRETREATMENT == null || MONITOR==null
                || CONTROLLER_PORT == -1) {
            return false;
        }
        return true;
    }

    public void printConfInformation() {
        System.out.println("=========================FaultFuzz Configuration=========================");
        System.out.println("Controller port: "+CONTROLLER_PORT);
        System.out.println("Configuration file: "+FAV_TRIGGER_CONFIG.getAbsolutePath());
        System.out.println("Root data path: "+FileUtil.root);
        System.out.println("Current crash point file: "+CUR_FAULT_FILE.getAbsolutePath());
        System.out.println("Prepare cluster script: "+(PRETREATMENT==null?"":PRETREATMENT.getAbsolutePath()));
        
        String workloadPathStr = "";
        if(WORKLOADLIST != null && WORKLOADLIST.size() > 0) {
            for(int i = 0; i < WORKLOADLIST.size(); i++) {
                if(i == 0) {
                    workloadPathStr += WORKLOADLIST.get(i).getAbsolutePath();
                } else {
                    workloadPathStr += ", "+WORKLOADLIST.get(i).getAbsolutePath();
                }
            }
        }
        
        System.out.println("Workload script: "+ workloadPathStr);
        System.out.println("Checker script: "+CHECKER.getAbsolutePath());
        System.out.println("Crash node script: "+CRASH.getAbsolutePath());
        System.out.println("Reboot node script: "+(REBOOT==null?"":REBOOT.getAbsolutePath()));
        System.out.println("Monitor script: "+(MONITOR==null?"":MONITOR.getAbsolutePath()));
        System.out.println("Max test time: "+FileUtil.parseSecondsToStringTime(this.maxTestMinutes*60));
        System.out.println("Hang timeout: "+FileUtil.parseSecondsToStringTime(this.hangSeconds));
        System.out.println("Max fault number: "+this.MAX_FAULTS);
        System.out.println("Fault constraints: ");
        for(MaxDownNodes group:maxDownGroup) {
        	System.out.println("For nodes "+group.aliveGroup+", allowed max down nodes at same time is:"+group.maxDown);
        }

        System.out.println("Network_Disconnection script: " + NETWORK_DISCONNECTION.getAbsolutePath());
        System.out.println("Network_Reconnection script: " + NETWORK_RECONNECTION.getAbsolutePath());
        System.out.println("evaluate target: " + EVALUATE_TARGET.toString());
        System.out.println("log level: " + LOG_LEVEL.toString());

        System.out.println("=======================================================================");
    }
}
