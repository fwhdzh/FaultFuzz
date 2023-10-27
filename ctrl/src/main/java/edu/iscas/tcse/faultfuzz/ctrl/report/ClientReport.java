package edu.iscas.tcse.faultfuzz.ctrl.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.FuzzInfo;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class ClientReport {

    public static String generateClientReport() {
    	SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间 
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记  
        Date date = new Date();// 获取当前时间 
        String time = sdf.format(date);
    	String rst = "";
    	rst += "*********************************************************************************\n";
    	rst += "*******************************FaultFuzz Result**********************************\n";
    	rst += "*******************************"+time+"**********************************\n";
    	rst += "*********************************************************************************\n";
    	rst += "*Tested time: "+FileUtil.parseSecondsToStringTime(FuzzInfo.getUsedSeconds())+"\n";
    	rst += "*For "+FuzzInfo.total_execs+" performed tests, the total execution time is "
    	+FileUtil.parseSecondsToStringTime(FuzzInfo.exec_us)+", the average execution time is "
    			+(FuzzInfo.total_execs==0?"0":FileUtil.parseSecondsToStringTime(FuzzInfo.exec_us/FuzzInfo.total_execs))+"\n";
    	rst += "*For "+FuzzInfo.total_bitmap_entries+" collected maps, the total map size is "
    					+FuzzInfo.total_bitmap_size+", the average size of every map is "
    			+(FuzzInfo.total_bitmap_entries==0?"0":(FuzzInfo.total_bitmap_size/FuzzInfo.total_bitmap_entries))
    			+"\n";
    	rst += "*Skip "+FuzzInfo.total_skipped+" tests, including "+FuzzInfo.total_nontrigger+" not triggered cases.\n";
    	rst += "*Test "+FuzzInfo.testedUniqueCases.size()+" unique cases (different fault sequence IDs).\n";
    	rst += "---------------------------------------------------------------------------------\n";
    	rst += "*Got "+FuzzInfo.total_bugs+" bugs.\n";
    	rst += "*Got "+FuzzInfo.total_hangs+" hangs.\n";
    	rst += "*********************************************************************************\n";
    	rst += "********************************COVERAGE*****************************************\n";
    	rst += "last new coverate: "+FileUtil.parseSecondsToStringTime(FuzzInfo.lastNewCovTime)
    	+"("+FuzzInfo.lastNewCovFaults+" faults)\n";
    	List<Integer> sortedKeys = new ArrayList<Integer>();
    	sortedKeys.addAll(FuzzInfo.timeToTotalCovs.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	for(Integer key:sortedKeys) {
    		rst += "for "+key+"th "+FuzzInfo.reportWindow+" minutes, the total coverage is "+FuzzInfo.timeToTotalCovs.get(key)+"\n";
    	}
    	int total = 0;
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
    	if(!sortedKeys.isEmpty()) {
    		rst += "*****************************TEST COUNT******************************************\n";
    	}
    	for(Integer key:sortedKeys) {
    		rst += "for "+key+"th "+FuzzInfo.reportWindow+" minutes: \n";
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToTestsNum.get(key);
    		int count = 0;
    		for(Integer faults:value.keySet()) {
    			faultToExec.computeIfAbsent(faults, k->0);
    			faultToExec.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    			rst += "--"+value.get(faults)+" "+faults+"-faults tests were executed. \n";
    			count += value.get(faults);
    		}
    		total += count;
    		rst += "**"+count+" tests were executed in this time window. \n";
    		rst += "**"+total+" tests were executed in total for now. \n";
    		for(Integer i:faultToExec.keySet()) {
    			rst += "**"+faultToExec.get(i)+" "+i+"-faults tests were executed in total for now. \n";
    		}
    		rst += "---------------------------------------------------------------------------------\n";
    	}
    	total = 0;
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToNewCovTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
    	if(!sortedKeys.isEmpty()) {
    		rst += "******************************NEW COV TEST COUNT*********************************\n";
    	}
    	for(Integer key:sortedKeys) {
    		rst += "for "+key+"th "+FuzzInfo.reportWindow+" minutes: \n";
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToNewCovTestsNum.get(key);
    		int count = 0;
    		for(Integer faults:value.keySet()) {
    			faultToNewCovs.computeIfAbsent(faults, k->0);
    			faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    			rst += "--"+value.get(faults)+" "+faults+"-faults tests resulted in new coverages. \n";
    			count += value.get(faults);
    		}
    		rst += "**"+count+" tests resulted in new coverages in this time window. \n";
    		total += count;
    		rst += "**"+total+" tests resulted in new coverages in total for now. \n";
    		for(Integer i:faultToNewCovs.keySet()) {
    			rst += "**"+faultToNewCovs.get(i)+" "+i+"-faults tests resulted in new coverages in total for now. \n";
    		}
    		rst += "---------------------------------------------------------------------------------\n";
    	}
    	rst += "***********************************BUG COUNT*************************************\n";
    	rst += "last new bug: "+FileUtil.parseSecondsToStringTime(FuzzInfo.lastNewBugTime)
    	+"("+FuzzInfo.lastNewBugFaults+" faults)\n";
    	total = 0;
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsBugsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToBugs = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		rst += "for "+key+"th "+FuzzInfo.reportWindow+" minutes: \n";
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsBugsNum.get(key);
    		int count = 0;
    		for(Integer faults:value.keySet()) {
    			faultToBugs.computeIfAbsent(faults, k->0);
    			faultToBugs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    			rst += "--"+value.get(faults)+" "+faults+"-faults tests caused bugs. \n";
    			count += value.get(faults);
    		}
    		rst += "**"+count+" tests caused bugs in this time window. \n";
    		total += count;
    		rst += "**"+total+" tests caused bugs in total for now. \n";
    		for(Integer i:faultToBugs.keySet()) {
    			rst += "**"+faultToBugs.get(i)+" "+i+"-faults tests caused bugs in total for now. \n";
    		}
    		rst += "---------------------------------------------------------------------------------\n";
    	}
    	rst += "***********************************HANG COUNT************************************\n";
    	rst += "last new hang: "+FileUtil.parseSecondsToStringTime(FuzzInfo.lastNewHangTime)
    	+"("+FuzzInfo.lastNewHangFaults+" faults)\n";
    	total = 0;
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsHangsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToHangs = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		rst += "for "+key+"th "+FuzzInfo.reportWindow+" minutes: \n";
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsHangsNum.get(key);
    		int count = 0;
    		for(Integer faults:value.keySet()) {
    			faultToHangs.computeIfAbsent(faults, k->0);
    			faultToHangs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    			rst += "--"+value.get(faults)+" "+faults+"-faults tests caused hangs. \n";
    			count += value.get(faults);
    		}
    		rst += "**"+count+" tests caused hangs in this time window. \n";
    		total += count;
    		rst += "**"+total+" tests caused hangs in total for now. \n";
    		for(Integer i:faultToHangs.keySet()) {
    			rst += "**"+faultToHangs.get(i)+" "+i+"-faults tests caused hangs in total for now. \n";
    		}
    		rst += "---------------------------------------------------------------------------------\n";
    	}
    	rst += "*************************************END*****************************************\n";
    	return rst;
    }
    
}
