package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;

public class MaxDownNodes{
    
	public int maxDown;
	public Set<String> aliveGroup;
	public Set<String> deadGroup;

    public static void buildClusterStatus(List<MaxDownNodes> currentCluster, String faultNodeIp, FaultType faultType) {
        for (MaxDownNodes subCluster : currentCluster) {
            // System.out.println("mutation, maxDown "+subCluster.maxDown
            // +", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
            if (subCluster.aliveGroup.contains(faultNodeIp) && faultType.equals(FaultType.CRASH)) {
                subCluster.maxDown--;
                subCluster.aliveGroup.remove(faultNodeIp);
                subCluster.deadGroup.add(faultNodeIp);

                // System.out.println("mutation, move "+faultNodeIp+" from alive to
                // dead."+subCluster.maxDown);
                break;
            } else if (subCluster.deadGroup.contains(faultNodeIp) && faultType.equals(FaultType.REBOOT)) {
                subCluster.maxDown++;
                subCluster.deadGroup.remove(faultNodeIp);
                subCluster.aliveGroup.add(faultNodeIp);
                // System.out.println("mutation, move "+faultNodeIp+" from dead to
                // alive."+subCluster.maxDown);
                break;
            } else {
                continue;
            }
        }
    }
    
    public static boolean isAliveNode(List<MaxDownNodes> currentCluster, String faultNodeIp) {
        for (MaxDownNodes subCluster : currentCluster) {
            // System.out.println("mutation, maxDown "+subCluster.maxDown
            // +", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
            if (subCluster.aliveGroup.contains(faultNodeIp)) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    public static boolean isDeadNode(List<MaxDownNodes> currentCluster, String faultNodeIp) {
    		for(MaxDownNodes subCluster:currentCluster) {
    //			System.out.println("mutation, maxDown "+subCluster.maxDown
    //					+", alive:"+subCluster.aliveGroup+", dead:"+subCluster.deadGroup);
    			if(subCluster.deadGroup.contains(faultNodeIp)) {
    				return true;
    			} else {
    				continue;
    			}
    		}
    		return false;
    	}

    public static List<MaxDownNodes> cloneCluster(List<MaxDownNodes> srcCluster) {
    	List<MaxDownNodes> desCluster = new ArrayList<MaxDownNodes>();
    	for(MaxDownNodes sub:srcCluster) {
    		MaxDownNodes group = new MaxDownNodes();
    		group.maxDown = sub.maxDown;
    		group.aliveGroup = new HashSet<String>();
    		group.aliveGroup.addAll(sub.aliveGroup);
    		
    		group.deadGroup = new HashSet<String>();
    		group.deadGroup.addAll(sub.deadGroup);
    		desCluster.add(group);
    	}
    	return desCluster;
    }

}