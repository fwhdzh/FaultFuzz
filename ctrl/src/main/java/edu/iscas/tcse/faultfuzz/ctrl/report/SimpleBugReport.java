package edu.iscas.tcse.faultfuzz.ctrl.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.FuzzInfo;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class SimpleBugReport {

    public static String generateSimpleReport() {
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
    	rst += "last new coverate: "+FileUtil.parseSecondsToStringTime(FuzzInfo.lastNewCovTime)
    	+"("+FuzzInfo.lastNewCovFaults+" faults)\n";
    
    	List<Integer> sortedKeys = new ArrayList<Integer>();
    	sortedKeys.addAll(FuzzInfo.timeToTotalCovs.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	if (!sortedKeys.isEmpty()) {
    		rst += "*****************************TOTAL COVERAGE******************************************\n";
    		int lastKey = sortedKeys.get(sortedKeys.size()-1);
    		rst += "total coverage: "+ FuzzInfo.timeToTotalCovs.get(lastKey)+"\n";
    	}
    
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
    	if(!sortedKeys.isEmpty()) {
    		rst += "*****************************TEST COUNT******************************************\n";
    	}
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToTestsNum.get(key);
    		for(Integer faults:value.keySet()) {
    			faultToExec.computeIfAbsent(faults, k->0);
    			faultToExec.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    		}
    	}
    	for (int i = 0; i < faultToExec.size(); i++) {
    		rst += "--"+faultToExec.get(i)+" "+i+"-faults tests were executed in total for now. \n";
    	}
    
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToNewCovTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
    	if(!sortedKeys.isEmpty()) {
    		rst += "******************************NEW COV TEST COUNT*********************************\n";
    	}
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToNewCovTestsNum.get(key);
    		for(Integer faults:value.keySet()) {
    			faultToNewCovs.computeIfAbsent(faults, k->0);
    			faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    		}
    	}
    	for (int i = 0; i < faultToNewCovs.size(); i++) {
    		rst += "--"+faultToNewCovs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
    	}
    
    	rst += "***********************************BUG COUNT*************************************\n";
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsBugsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToBugs = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsBugsNum.get(key);
    		for(Integer faults:value.keySet()) {
    			faultToBugs.computeIfAbsent(faults, k->0);
    			faultToBugs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    		}
    	}
    	for (int i = 0; i < faultToBugs.size(); i++) {
    		rst += "--"+faultToBugs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
    	}
    
    	rst += "***********************************HANG COUNT************************************\n";
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsHangsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToHangs = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsHangsNum.get(key);
    		for(Integer faults:value.keySet()) {
    			faultToHangs.computeIfAbsent(faults, k->0);
    			faultToHangs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    		}
    	}
    
    	for (int i = 0; i < faultToHangs.size(); i++) {
    		rst += "--"+faultToHangs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
    	}
    
    	rst += "*************************************END*****************************************\n";
    	return rst;
    
    }
    
}
