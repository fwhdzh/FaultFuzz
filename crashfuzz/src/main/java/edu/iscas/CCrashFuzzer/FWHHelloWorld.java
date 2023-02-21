package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
        fuzzer.recoveryFuzzInfoWithFWHString();
    }
}
