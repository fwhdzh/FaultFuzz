package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;

public class FWHHelloWorld {
    public static void main(String[] args) throws IOException {
        Stat.log("Hello world!");
        
        // File f = new File("/data/fengwenhan/data/crashfuzz_fwh/fwhinput.txt");
        // FileInputStream in = new FileInputStream(f);
        // List<String> oriList =  Files.readAllLines(f.toPath());
        // for (String s: oriList) {
        //     // System.out.println(s);
        //     QueueEntry q = JSON.parseObject(s, QueueEntry.class);
        //     String t = JSONObject.toJSONString(q.faultSeq);
            

        //     System.out.println(t);
        //     List<IOPoint> ioList = q.ioSeq;
        //     System.out.println(ioList.get(0));
        //     System.out.println(ioList.get(1));
            
        //     for (FaultPoint fp : q.faultSeq) {
        //         for (IOPoint p: ioList) {
        //             if (p.CALLSTACK.equals(fp.ioPt.CALLSTACK)) {
        //                 System.out.println(p.CALLSTACK.toString());
        //                 System.out.println(fp.ioPt.CALLSTACK.toString());
        //                 List<Integer> list = new ArrayList<>();
        //                 list.add(1);
        //                 System.out.println(list.toString());
        //                 System.out.println("check success");
        //             }
        //         }
        //     }
            
        // }

        Fuzzer fuzzer = new Fuzzer(null, null, false);
        // fuzzer.recoveryFuzzInfoWithFWHString();
        // Set<Integer> a = new HashSet<>();
        // a.add(1);
        // a.add(2);
        // a.add(3);
        // QueueManagerNew.tested_fault_id = a;
        // fuzzer.recordTestedFaultId();
        // fuzzer.recoveryTestedFaultId();
        // fuzzer.recovery();
        
    }

    private void testJSONRefs() {
        IOPoint i1 = new IOPoint();
        List<IOPoint> ioSeq = new ArrayList<>();
        ioSeq.add(i1);
        ioSeq.add(i1);
        String s = JSONObject.toJSONString(ioSeq);
        System.out.println(s);
        List<IOPoint> rSeq = new ArrayList<>();
        rSeq = JSON.parseArray(s, IOPoint.class);
        System.out.println();
        System.out.println(JSONObject.toJSONString(rSeq));
        System.out.println(JSONObject.toJSONString(rSeq.get(1)));
        System.out.println(rSeq.get(0) ==  rSeq.get(1));

        String s2 = JSONObject.toJSONString(ioSeq, SerializerFeature.DisableCircularReferenceDetect);
        List<IOPoint> rSeq2 = JSON.parseArray(s2, IOPoint.class);
        System.out.println(s2);
        System.out.println(rSeq2.get(0) ==  rSeq2.get(1));

        IOPoint i2 = new IOPoint();
        IOPoint i3 = new IOPoint();
        System.out.println();
        System.out.println(i2 == i3);
    }
}
