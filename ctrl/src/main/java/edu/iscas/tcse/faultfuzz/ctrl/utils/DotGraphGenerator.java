package edu.iscas.tcse.faultfuzz.ctrl.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class DotGraphGenerator {

    private String getCallStackString(List<String> cList) {
        String result = "\\n";
        for (String s: cList) {
            result = result + s + "\\l";
        }
        return result;
    }

    public String generateIOListGraph(List<IOPoint> iList) {
        String result =  generateIOListGraph(iList, true);
        return result;
    } 

    public String generateIOListGraph(List<IOPoint> iList, boolean withCallStack) {
        String result = "digraph mygraph {\n";
        if (withCallStack) {
            for (IOPoint p: iList) {
                String s =  "" + p.ioID + "[label=\"" + p.ioID + "\\n" + getCallStackString(p.CALLSTACK) + "\"]\n";
                result += s;
            }
        }
        for (int i = 0; i < iList.size()-1; i++) {
            String s = "\"" + iList.get(i).ioID + "\" -> \"" + iList.get(i+1).ioID + "\"";
            s = s + "[label=\"" + i  + "\"]";
            s = s + "\n";
            result += s;
        }
        result += "}\n";
        return result;
    }



    public String generateTimeSequenceList(String key, List<IOPoint> iList) {
        String result = "subgraph " + "\"cluster_" + key + "\"" + " {\n";
        result += "label = \"" + key + "\";\n";
        // result += "style=solid;\n";
        for (int i = 0; i < iList.size(); i++) {
            IOPoint p = iList.get(i);
            String s =  "" + "\"" + key + "-" + i + "\"" + "[label=\"" +  key + "\\n"+  p.ioID + "\\n" + "time: " + p.TIMESTAMP + "\\lpath: " + p.PATH + "\\l" + "\"]\n";
            result += s;
        }
        
        for (int i = 0; i < iList.size()-1; i++) {
            String s = "\"" + key + "-" + i + "\" -> \"" + key + "-" + (i+1) + "\"";
            s = s + "[label=\"" + i  + "\"]";
            s = s + "\n";
            result += s;
        }
        result += "}\n";
        return result;
    }

    public Map<String, String> findMsgPair(Map<String, List<IOPoint>> m) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<IOPoint>> entry: m.entrySet() ) {
            List<IOPoint> sendNodeList = entry.getValue();
            for (int i = 0; i < sendNodeList.size(); i++) {
                IOPoint p = sendNodeList.get(i);
                if (p.PATH.contains("FAVMSG:") && (!p.PATH.contains("FAVMSG:READ"))) {
                    String recieveNode = p.PATH.split("FAVMSG:")[1].split("&")[0];
                    String msgId = p.PATH.split("&")[1];
                    String addedKey = "\"" + entry.getKey() + "-" + i + "\"";
                    String addedValue = null; 
                    boolean flag = false;
                    // System.out.println(recieveNode);
                    List<IOPoint> recieveNodeList = m.get(recieveNode);
                    for (int j = 0; j < recieveNodeList.size(); j++) {
                        IOPoint jp = recieveNodeList.get(j);
                        if (jp.PATH.contains("FAVMSG:READ")) {
                            String jpSendNode = jp.PATH.split("FAVMSG:READ")[1].split("&")[0];
                            if (jpSendNode.equals(entry.getKey()) && msgId.equals(jp.PATH.split("&")[1])) {
                                flag = true;
                                addedValue = "\"" + recieveNode + "-" + j + "\"";
                                break; 
                            }
                        }
                    }
                    result.put(addedKey, addedValue);
                }
            }
        }
        return result;
    }

    public String generateEdgeOfMsgPairs(Map<String, String> m) {
        String result = "";
        int notRecieveCount = 0;
        for (Map.Entry<String, String> entry: m.entrySet()) {
            if (entry.getValue() == null) {
                notRecieveCount++;
                continue;
            }
            result = result + entry.getKey() + " -> " + entry.getValue() + "\n";
        }
        System.out.println("There are " + notRecieveCount + " messages are not recieved!");
        return result;
    }

    public Map<Long, List<String>> findPointsIdWithSameTimeStamp(List<IOPoint> iList) {
        Map<Long, List<String>> result = new HashMap<>();
        Map<String, Integer> nodeCountMap = new HashMap<>();
        for (IOPoint p: iList) {
            Long l = Long.valueOf(p.TIMESTAMP);
            if (!result.containsKey(l)) {
                result.put(l, new ArrayList<>());
            }
            String node = p.ip;
            if (!nodeCountMap.containsKey(node)) {
                nodeCountMap.put(node, 0);
            }
            String s = "\"" + node + "-" + nodeCountMap.get(node) + "\"";
            result.get(l).add(s);
            int t = nodeCountMap.get(node);
            nodeCountMap.put(node, t+1);
        }
        return result;
    }

    public Map<Long, List<String>> removeNotSuitedElements(Map<Long, List<String>> m) {
        Map<Long, List<String>> result = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry: m.entrySet()) {
            if (entry.getValue().size() <= 1) {
                continue;
            }
            Map<String, Integer> nodeCount = new HashMap<>();
            for (String t: entry.getValue()) {
                String s = t.substring(1, t.length()-1);
                String node = s.split("-")[0];
                String indexS = s.split("-")[1];
                Integer index = Integer.parseInt(indexS);
                nodeCount.computeIfAbsent(node, key -> -1);
                nodeCount.computeIfPresent(node, (key, value) -> value < index ? index : value);
            }
            List<String> vList = new ArrayList<>();
            for (Map.Entry<String, Integer> innerEntry: nodeCount.entrySet()) {
                String s = innerEntry.getKey() + "-" + innerEntry.getValue();
                s = "\"" + s + "\"";
                vList.add(s);
            }
            if (vList.size() > 1) {
                result.put(entry.getKey(), vList);
            }
        }
        return result;
    }

    public Map<Long, List<IOPoint>> findIOPointsWithSameTimeStamp(List<IOPoint> iList) {
        Map<Long, List<IOPoint>> result = new HashMap<>();
        for (IOPoint p: iList) {
            Long l = Long.valueOf(p.TIMESTAMP);
            if (!result.containsKey(l)) {
                result.put(l, new ArrayList<>());
            }
            result.get(l).add(p);
        }
        return result;
    }

    public String generateSameRankOfIOPoints(Map<Long, List<String>> m) {
        String result = "";
        for (Map.Entry<Long, List<String>> entry: m.entrySet()) {
            String s = "{ rank=same; ";
            for (String ps: entry.getValue()) {
                s = s + ps + "; ";
            }
            s += "}\n";
            result += s;
        }
        return result;
    }

    public Map<Long, List<String>> makeFisrtIOPointSameRank() {
        Map<Long, List<String>> result = new HashMap<>();
        List<String> vList = new ArrayList<>();
        vList.add("\"172.30.0.2-0\"");
        vList.add("\"172.30.0.3-0\"");
        vList.add("\"172.30.0.4-0\"");
        vList.add("\"172.30.0.5-0\"");
        vList.add("\"172.30.0.6-0\"");
        result.put(new Long(0), vList);
        return result;
    }
    
    public String generateTimeSequenceGraph(Map<String, List<IOPoint>> m, List<IOPoint> iList) {
        String result = "digraph mygraph {\n";
        for (Map.Entry<String, List<IOPoint>> entry: m.entrySet() ) {
            result += generateTimeSequenceList(entry.getKey(), entry.getValue());
        }
        Map<String, String> msgPairs = findMsgPair(m);
        result += generateEdgeOfMsgPairs(msgPairs);
        Map<Long, List<String>> sameTimeMap = findPointsIdWithSameTimeStamp(iList);
        // Map<Long, List<String>> modifiedMap = removeNotSuitedElements(sameTimeMap);
        Map<Long, List<String>> modifiedMap = makeFisrtIOPointSameRank();
        result += generateSameRankOfIOPoints(modifiedMap);
        result += "}\n";
        return result;
    }

    public String generateTimeSequenceGraph(List<IOPoint> iList) {
        Map<String, List<IOPoint>> m = FaultSequenceUtil.divedeTheIOListByNode(iList);
        return generateTimeSequenceGraph(m, iList);
    }

    public void generateTimeSequenceGraph(List<IOPoint> iList, String targetPath) {
        String s = generateTimeSequenceGraph(iList);
        generateGraphWithString(s, targetPath);
    }

    public void generateGraphWithString(String s, String targetPath) {
        
        FileOutputStream out;
		try {
			out = new FileOutputStream(targetPath, false);
			out.write(s.getBytes());
			out.write("\n".getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateIOListGraph(List<IOPoint> iList, String targetPath) {
        String s = generateIOListGraph(iList);
        generateGraphWithString(s, targetPath);

    }
    
}
