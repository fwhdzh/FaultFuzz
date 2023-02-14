package edu.iscas.CCrashFuzzer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.iscas.CCrashFuzzer.utils.FileUtil;

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
    public File CUR_CRASH_FILE;
    public File UPDATE_CRASH;
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
	
	public static class MaxDownNodes{
		public int maxDown;
		public Set<String> aliveGroup;
		public Set<String> deadGroup;
	}
	
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

        String curCrashFile = p.getProperty(ConfOption.CUR_CRASH_FILE.toString());
        if(curCrashFile != null) {
            if(!curCrashFile.startsWith("/")) {
            	curCrashFile = workdir + curCrashFile;
            }
        	File f = new File(curCrashFile);
        	CUR_CRASH_FILE = f;
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

        String updateCurCrash = p.getProperty(ConfOption.UPDATE_CRASH.toString());
        if(updateCurCrash != null) {
            if(!updateCurCrash.startsWith("/")) {
            	updateCurCrash = workdir + updateCurCrash;
            }
            File f = new File(updateCurCrash);
            if(f.exists()) {
                UPDATE_CRASH = f;
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

        if(RESTART == null || CRASH == null || WORKLOAD == null || CUR_CRASH_FILE == null || PRETREATMENT == null) {
        	throw new IOException();
        }

        System.out.println("=========================CrashFuzzer Configuration=========================");
        System.out.println("Controller port: "+CONTROLLER_PORT);
        System.out.println("Configuration file: "+FAV_TRIGGER_CONFIG.getAbsolutePath());
        System.out.println("Root report path: "+FileUtil.root);
        System.out.println("Current crash point file: "+CUR_CRASH_FILE.getAbsolutePath());
        System.out.println("Update current crash point script: "+UPDATE_CRASH.getAbsolutePath());
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
        System.out.println("=======================================================================");
    }
}
