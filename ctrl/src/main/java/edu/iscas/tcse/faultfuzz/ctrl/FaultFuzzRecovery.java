package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class FaultFuzzRecovery {

    public Conf conf;

    public FaultFuzzRecovery(Conf conf) {
        this.conf = conf;
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

    public void recoverCandidateQueue(String rootPath) throws IOException {

        File root = new File(rootPath);
        File[] files = root.listFiles();
        //get "persist" subfolder
        File persistFolder = null;
        for (File file : files) {
            if (file.getName().equals("persist")) {
                persistFolder = file;
                break;
            }
        }
        File[] persistTestFiles = persistFolder.listFiles();
        for (File file : persistTestFiles) {
            System.out.println(file.getName());
        }


        
        Map<String, QueueEntry> queueEntryMap = new HashMap<>();
        for (File file : persistTestFiles) {
            QueueEntry entry = FileUtil.retriveReplayQueueEntryFromRSTFolder(file.getAbsolutePath());
            entry.fname = file.getName();
            queueEntryMap.put(file.getName(), entry);
        }

        for (String entryId : queueEntryMap.keySet()) {
            QueueEntry entry = queueEntryMap.get(entryId);
            File seedFile = new File(persistFolder.getAbsolutePath() + "/" + entryId +"/SEED");
            String seed;
            // read s from seedFile
            List<String> lines = Files.readAllLines(seedFile.toPath());
            // init-0 SEED file is empty.
            if (lines.size() > 0) {
                seed = lines.get(0);
                System.out.println(entryId + "s' seed is: " + seed);
                if (queueEntryMap.containsKey(seed)) {
                    entry.seed = queueEntryMap.get(seed);
                } else {
                    throw new RuntimeException("A fault sequence's seed cannot be founded.");
                }
            } 
        }

        for (String entryId : queueEntryMap.keySet()) {
            QueueEntry entry = queueEntryMap.get(entryId);
            Mutation.initializeFaultPointsToMutate(entry, conf.MAX_FAULTS, conf.maxDownGroup);
		    Mutation.initializeLocalNotTestedFaultId(entry);
		    Mutation.mutateFaultSequence(entry);
        }

        for (String entryId : queueEntryMap.keySet()) {
            QueueEntry entry = queueEntryMap.get(entryId);
            if (entry.seed != null) {
                entry.seed.mutates.remove(entry);
            }
        }

        List<QueueEntry> candidateQueue = new ArrayList<>();

        for (String entryId : queueEntryMap.keySet()) {
            QueueEntry entry = queueEntryMap.get(entryId);
            if (entry.mutates != null) {
                candidateQueue.add(entry);
            }
        }

        System.out.println("candidateQueue size: " + candidateQueue.size());
    }

    public void recordVirginBits(String filepath) {
    	CoverageCollector.write_bitmap(CoverageCollector.virgin_bits, filepath);
    }

}
