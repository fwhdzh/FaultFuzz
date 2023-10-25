package edu.iscas.tcse.faultfuzz.ctrl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class FaultSequenceUtil {
    public static Map<String, List<IOPoint>> divedeTheIOListByNode(List<IOPoint> iList) {
        Map<String, List<IOPoint>> result = new HashMap<>();
        for (IOPoint ioPoint : iList) {
            String ip = ioPoint.ip;
            // Integer i = Integer.valueOf(ioID);
            result.computeIfAbsent(ip, key -> new ArrayList<IOPoint>());
            result.get(ip).add(ioPoint);
        }
        return result;
    }
}
