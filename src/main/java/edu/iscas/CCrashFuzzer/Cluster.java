package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;

public class Cluster {
	//public RestartDecision restartDecision;
	public Conf favconfig;
	public Cluster(Conf favconfig) {
		this.favconfig = favconfig;
	}

	public ArrayList<String> prepareCluster() {
		System.out.println("prepare:"+favconfig.PRETREATMENT);
		System.out.println("workload:"+favconfig.WORKLOAD);
		if(favconfig.PRETREATMENT != null) {
			String path = favconfig.PRETREATMENT.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}

	public ArrayList<String> runWorkload() {
		if(favconfig.WORKLOAD != null) {
			String path = favconfig.WORKLOAD.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}

	public ArrayList<String> runChecker(boolean crashed, boolean restarted, String crashNIP) {
        if(favconfig.CHECKER != null) {
            String path = favconfig.CHECKER.getAbsolutePath();
            String workingDir = path.substring(0, path.lastIndexOf("/"));
            return RunCommand.run(path+" "+crashed+" "+restarted+" "+crashNIP, workingDir);
            //return RunCommand.run(path);
        } else {
            return null;
        }
    }

	public Collection<? extends String> runChecker(Conf conf, List<MaxDownNodes> currentCluster, String runInfoDir) {
		// TODO Auto-generated method stub
		if(favconfig.CHECKER != null) {
            String path = favconfig.CHECKER.getAbsolutePath();
            String workingDir = path.substring(0, path.lastIndexOf("/"));
            
            Set<String> aliveNodes = new HashSet<String>();
            Set<String> deadNodes = new HashSet<String>();
            for(MaxDownNodes subC:currentCluster) {
            	aliveNodes.addAll(subC.aliveGroup);
            	deadNodes.addAll(subC.deadGroup);
            }
            String alive = "NULL";
            String dead = "NULL";
            if(!aliveNodes.isEmpty()) {
            	alive = aliveNodes.toString().substring(1, aliveNodes.toString().length()-1).replaceAll(" ", "");
            }
            if(!deadNodes.isEmpty()) {
            	dead = deadNodes.toString().substring(1, deadNodes.toString().length()-1).replaceAll(" ", "");
            }

            //checker.sh [alive_ip1, alive_ip2, alive_ip3] [dead_ip1, dead_ip2]
            return RunCommand.run(path+" "
                    +alive
                    +" "
            		+dead
            		+" "
            		+runInfoDir, workingDir);
            //return RunCommand.run(path);
        } else {
            return null;
        }
	}

	public ArrayList<String> killNode(String acceptedCrashNode, String nodeName) {
		if(favconfig.CRASH != null) {
			String path = favconfig.CRASH.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+acceptedCrashNode+" "+nodeName, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}

	public ArrayList<String> restartNode(String nodeName) {
		//restartDecision.restart(point, async);
		if(favconfig.RESTART != null) {
			String path = favconfig.RESTART.getAbsolutePath();
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+nodeName, workingDir);
			//return RunCommand.run(path+" "+nodeName);
		} else {
			return null;
		}
	}
}
