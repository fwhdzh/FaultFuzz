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

public class Conf {
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
	public long hangMinutes = 10;
	public static int MAP_SIZE = 10000;
	
	public class MaxDownNodes{
		int maxDown;
		Set<String> nodesGroup;
	}
	
	public Conf(File configFile) {
		FAV_TRIGGER_CONFIG = configFile;
	}

    public void loadConfiguration() throws IOException {
    	InputStream in = new BufferedInputStream(new FileInputStream(FAV_TRIGGER_CONFIG));
        Properties p = new Properties();
        p.load(in);

        String workload = p.getProperty(ConfOption.WORKLOAD.toString());
        if(workload != null) {
        	File f = new File(workload);
            if(f.exists()) {
            	WORKLOAD = f;
            } else {
            	throw new IOException();
            }
        }

        String curCrashFile = p.getProperty(ConfOption.CUR_CRASH_FILE.toString());
        if(curCrashFile != null) {
        	File f = new File(curCrashFile);
        	CUR_CRASH_FILE = f;
        }
        
        String mapSize = p.getProperty(ConfOption.MAP_SIZE.toString());
        if(mapSize != null) {
        	MAP_SIZE = Integer.parseInt(mapSize);
        }
        
        String testTime = p.getProperty(ConfOption.TEST_TIME.toString());
        if(testTime != null) {
        	if(testTime.endsWith("s")) {
        		maxTestMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("s")))/60;
        	} else if (testTime.endsWith("m")) {
        		maxTestMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("m")));
        	} else if (testTime.endsWith("h")) {
        		maxTestMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("h")))*60;
        	}
        }
        
        String hangTMOut = p.getProperty(ConfOption.HANG_TMOUT.toString());
        if(hangTMOut != null) {
        	if(testTime.endsWith("s")) {
        		hangMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("s")))/60;
        	} else if (testTime.endsWith("m")) {
        		hangMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("m")));
        	} else if (testTime.endsWith("h")) {
        		hangMinutes = Long.parseLong(testTime.substring(0, testTime.lastIndexOf("h")))*60;
        	}
        }

        String faultConfig = p.getProperty(ConfOption.FAULT_CONFIG.toString());
        if(faultConfig != null) {
        	FAULT_CONFIG = faultConfig; //1:{ip1,ip2,ip3};2:{ip4,ip5}
        	maxDownGroup = new ArrayList<MaxDownNodes>();
        	String[] groups = FAULT_CONFIG.trim().split(";");
        	for(String group:groups) {
        		String[] secs = group.trim().split(":");
        		int maxDown = Integer.parseInt(secs[0]);
        		String[] ips = secs[1].trim().split(",");
        		Set<String> ipSet = new HashSet<String>();
        		for(String ip:ips) {
        			ipSet.add(ip.trim());
        		}
        		assert(maxDown<ipSet.size());
        		MaxDownNodes downGroup = new MaxDownNodes();
        		downGroup.maxDown = maxDown;
        		downGroup.nodesGroup = ipSet;
        		maxDownGroup.add(downGroup);
        	}
        }

        String pretreatment = p.getProperty(ConfOption.PRETREATMENT.toString());
        if(pretreatment != null) {
        	File f = new File(pretreatment);
            if(f.exists()) {
            	PRETREATMENT = f;
            }
        }

        String checker = p.getProperty(ConfOption.CHECKER.toString());
        if(checker != null) {
        	File f = new File(checker);
            if(f.exists()) {
            	CHECKER = f;
            }
        }

        String monitor = p.getProperty(ConfOption.MONITOR.toString());
        if(monitor != null) {
            File f = new File(monitor);
            if(f.exists()) {
                MONITOR = f;
            }
        }

        String updateCurCrash = p.getProperty(ConfOption.UPDATE_CRASH.toString());
        if(updateCurCrash != null) {
            File f = new File(updateCurCrash);
            if(f.exists()) {
                UPDATE_CRASH = f;
            }
        }

        String checkCrash = p.getProperty(ConfOption.CRASH.toString());
        if(checkCrash != null) {
        	File f = new File(checkCrash);
            if(f.exists()) {
            	CRASH = f;
            } else {
            	throw new IOException();
            }
        }

        String checkRestart = p.getProperty(ConfOption.RESTART.toString());
        if(checkRestart != null) {
        	File f = new File(checkRestart);
            if(f.exists()) {
            	RESTART = f;
            }
        }

        String report = p.getProperty(ConfOption.BUG_REPORT.toString());
        if(report != null) {
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
        System.out.println("Bug report path: "+FAV_BUG_REPORT.getAbsolutePath());
        System.out.println("Current crash point file: "+CUR_CRASH_FILE.getAbsolutePath());
        System.out.println("Update current crash point script: "+UPDATE_CRASH.getAbsolutePath());
        System.out.println("Prepare cluster script: "+(PRETREATMENT==null?"":PRETREATMENT.getAbsolutePath()));
        System.out.println("Workload script: "+WORKLOAD.getAbsolutePath());
        System.out.println("Checker script: "+CHECKER.getAbsolutePath());
        System.out.println("Crash node script: "+CRASH.getAbsolutePath());
        System.out.println("Restart node script: "+(RESTART==null?"":RESTART.getAbsolutePath()));
        System.out.println("Monitor script: "+(MONITOR==null?"":MONITOR.getAbsolutePath()));
        System.out.println("Fault constraints: ");
        for(MaxDownNodes group:maxDownGroup) {
        	System.out.println("For nodes "+group.nodesGroup+", allowed max down nodes at same time is:"+group.maxDown);
        }
        System.out.println("=======================================================================");
    }
}
