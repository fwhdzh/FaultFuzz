package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.selection.SelectionInfo;

public class RecoveryManagerFWH {

    // private Conf conf;

    // public RecoveryManagerFWH(Conf conf) {
    //     this.conf = conf;
    // }

	public void backUpLastRecord() {
		
	}

    public void recordCandidateQueue(String filepath) {
    	String message = JSONObject.toJSONString(Fuzzer.candidate_queue);
    	FileOutputStream out;
    	try {
    		out = new FileOutputStream(filepath, false);
    		out.write(message.getBytes());
    		out.write("\n".getBytes());
    		out.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordCandidateQueue(Fuzzer fuzzer) {
		recordCandidateQueue(fuzzer.conf.RECOVERY_CANDIDATEQUEUE_PATH);
	}

    public void recoverCandidateQueue(String filepath) {
    	File file = new File(filepath);
    	List<String> oriList;
    	try {
    		oriList = Files.readAllLines(file.toPath());
    		String s = oriList.get(0);
    		List<QueueEntry> c = JSON.parseArray(s, QueueEntry.class);
    		Stat.log(JSONObject.toJSONString(c));
    		Fuzzer.candidate_queue = c;
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    public void recoverCandidateQueue(Fuzzer fuzzer) {
    	recoverCandidateQueue(fuzzer.conf.RECOVERY_CANDIDATEQUEUE_PATH);
    }

    public void recordFuzzInfo(String filepath) {
    	String message = FuzzInfo.toJSONString();
    	FileOutputStream out;
    	try {
    		out = new FileOutputStream(filepath, false);
    		out.write(message.getBytes());
    		out.write("\n".getBytes());
    		out.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void recordFuzzInfo(Fuzzer fuzzer) {
    	recordFuzzInfo(fuzzer.conf.RECOVERY_FUZZINFO_PATH);
    }

    public void recoverFuzzInfo(String filepath) {
    	File file = new File(filepath);
    	List<String> oriList;
    	try {
    		oriList = Files.readAllLines(file.toPath());
    		String s = oriList.get(0);
    		FuzzInfoRecord record = JSON.parseObject(s, FuzzInfoRecord.class);
    		Stat.log(JSONObject.toJSONString(record));
    		record.copyToFuzzInfo();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }

    public void recoverFuzzInfo(Fuzzer fuzzer) {
    	recoverFuzzInfo(fuzzer.conf.RECOVERY_FUZZINFO_PATH);
    }

    public void recordTestedFaultId(String filepath) {
    	String message = JSONObject.toJSONString(SelectionInfo.tested_fault_id);
    	FileOutputStream out;
    	try {
    		out = new FileOutputStream(filepath, false);
    		out.write(message.getBytes());
    		out.write("\n".getBytes());
    		out.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordTestedFaultId(Fuzzer fuzzer) {
    	recordTestedFaultId(fuzzer.conf.RECOVERY_TESTEDFAULTID_PATH);
    }

    public void recoverTestedFaultId(String filepath) {
    	File file = new File(filepath);
    	List<String> oriList;
    	try {
    		oriList = Files.readAllLines(file.toPath());
    		String s = oriList.get(0);
    		Set<Integer> c = (Set)JSON.parseObject(s, Set.class);
    		Stat.log(JSONObject.toJSONString(c));
    		SelectionInfo.tested_fault_id = c;
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    public void recoverTestedFaultId(Fuzzer fuzzer) {
    	recoverTestedFaultId(fuzzer.conf.RECOVERY_TESTEDFAULTID_PATH);
    }

    public void recordQueue(String filepath, QueueEntry q) {
    	FileOutputStream out;
    	try {
    		out = new FileOutputStream(filepath, false);
    		out.write(q.toJSONString().getBytes());
    		out.write("\n".getBytes());
    		out.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void recordQueue(QueueEntry q) {
    	recordQueue("/data/fengwenhan/data/crashfuzz_fwh/QueueEntry.txt", q);
    }

    public void recordVirginBits(String filepath) {
    	CoverageCollector.write_bitmap(CoverageCollector.virgin_bits, filepath);
    }

    public void recordVirginBits(Fuzzer fuzzer) {
    	recordVirginBits(fuzzer.conf.RECOVERY_VIRGINBITS_PATH);
    }

    public void recoverVirginBits(String filepath) {
    	byte[] b = CoverageCollector.load_a_bitmap(filepath);
    	CoverageCollector.virgin_bits = b;
    }

    public void recoverVirginBits(Fuzzer fuzzer) {
    	recoverVirginBits(fuzzer.conf.RECOVERY_VIRGINBITS_PATH);
    }

}
