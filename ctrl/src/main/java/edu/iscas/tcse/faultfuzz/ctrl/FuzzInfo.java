package edu.iscas.tcse.faultfuzz.ctrl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class FuzzInfo {
	public static int reportWindow = 30; //30 minutes
	
    public static long startTime = System.currentTimeMillis();
    public static long last_used_seconds = 0;

    public static long total_execs = 0;
    public static long exec_us = 0;   
    
    public static int total_skipped = 0; //including not triggered ones
    public static int total_nontrigger = 0;
    public static int total_bugs = 0;
    public static int total_hangs = 0;
    
    public static Set<Integer> testedUniqueCases = new HashSet<Integer>();
    
    public static long total_bitmap_size = 0,         /* Total bit count for all bitmaps  */
    total_bitmap_entries = 0;      /* Number of bitmaps counted        */
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public static HashMap<Integer,Integer> timeToTotalCovs = new HashMap<Integer,Integer>();
    public static long lastNewCovTime = 0;
    public static int lastNewCovFaults = 0;
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsToNewCovTestsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsBugsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public static long lastNewBugTime = 0;
    public static int lastNewBugFaults = 0;
    
    public static HashMap<Integer,HashMap<Integer,Integer>> timeToFaulsHangsNum = new HashMap<Integer,HashMap<Integer,Integer>>();
    public static long lastNewHangTime = 0;
    public static int lastNewHangFaults = 0;

	public static int pauseSecond = 0;
    
    public static long getUsedSeconds() {
    	// return last_used_seconds + (((System.currentTimeMillis()-startTime)/ 1000));
		return last_used_seconds + (((System.currentTimeMillis()-startTime)/ 1000)) - pauseSecond;
    }

    public static int getTotalCoverage(byte[] bytes) {
    	int finds= 0;
		for(int i = 0; i< bytes.length; i++) {
			if(bytes[i]>0) {
				finds++;
			}
		}
		return finds;
    }

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
		rst += "last new coverate: "+FileUtil.parseSecondsToStringTime(lastNewCovTime)
		+"("+lastNewCovFaults+" faults)\n";

		List<Integer> sortedKeys = new ArrayList<Integer>();
		sortedKeys.addAll(timeToTotalCovs.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		if (!sortedKeys.isEmpty()) {
			rst += "*****************************TOTAL COVERAGE******************************************\n";
			int lastKey = sortedKeys.get(sortedKeys.size()-1);
			rst += "total coverage: "+ timeToTotalCovs.get(lastKey)+"\n";
		}

		int total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsToTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
		if(!sortedKeys.isEmpty()) {
			rst += "*****************************TEST COUNT******************************************\n";
		}
		for(Integer key:sortedKeys) {
			// rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsToTestsNum.get(key);
			// int count = 0;
			for(Integer faults:value.keySet()) {
				faultToExec.computeIfAbsent(faults, k->0);
				faultToExec.computeIfPresent(faults, (k, v) -> v + value.get(faults));
				// rst += "--"+value.get(faults)+" "+faults+"-faults tests were executed. \n";
				// count += value.get(faults);
			}
			// total += count;
			// rst += "**"+count+" tests were executed in this time window. \n";
			// rst += "**"+total+" tests were executed in total for now. \n";
			// for(Integer i:faultToExec.keySet()) {
			// 	rst += "**"+faultToExec.get(i)+" "+i+"-faults tests were executed in total for now. \n";
			// }
			// rst += "---------------------------------------------------------------------------------\n";
		}
		for (int i = 0; i < faultToExec.size(); i++) {
			rst += "--"+faultToExec.get(i)+" "+i+"-faults tests were executed in total for now. \n";
		}

		total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsToNewCovTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
		if(!sortedKeys.isEmpty()) {
			rst += "******************************NEW COV TEST COUNT*********************************\n";
		}
		for(Integer key:sortedKeys) {
			// rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsToNewCovTestsNum.get(key);
			// int count = 0;
			for(Integer faults:value.keySet()) {
				faultToNewCovs.computeIfAbsent(faults, k->0);
				faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
				// rst += "--"+value.get(faults)+" "+faults+"-faults tests resulted in new coverages. \n";
				// count += value.get(faults);
			}
			// rst += "**"+count+" tests resulted in new coverages in this time window. \n";
			// total += count;
			// rst += "**"+total+" tests resulted in new coverages in total for now. \n";
			// for(Integer i:faultToNewCovs.keySet()) {
			// 	rst += "**"+faultToNewCovs.get(i)+" "+i+"-faults tests resulted in new coverages in total for now. \n";
			// }
			// rst += "---------------------------------------------------------------------------------\n";
		}
		for (int i = 0; i < faultToNewCovs.size(); i++) {
			rst += "--"+faultToNewCovs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
		}

		rst += "***********************************BUG COUNT*************************************\n";
		// rst += "last new bug: "+FileUtil.parseSecondsToStringTime(lastNewBugTime)
		// +"("+lastNewBugFaults+" faults)\n";
		total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsBugsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToBugs = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			// rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsBugsNum.get(key);
			int count = 0;
			for(Integer faults:value.keySet()) {
				faultToBugs.computeIfAbsent(faults, k->0);
				faultToBugs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
				// rst += "--"+value.get(faults)+" "+faults+"-faults tests caused bugs. \n";
				// count += value.get(faults);
			}
			// rst += "**"+count+" tests caused bugs in this time window. \n";
			// total += count;
			// rst += "**"+total+" tests caused bugs in total for now. \n";
			// for(Integer i:faultToBugs.keySet()) {
			// 	rst += "**"+faultToBugs.get(i)+" "+i+"-faults tests caused bugs in total for now. \n";
			// }
			// rst += "---------------------------------------------------------------------------------\n";
		}
		for (int i = 0; i < faultToBugs.size(); i++) {
			rst += "--"+faultToBugs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
		}

		rst += "***********************************HANG COUNT************************************\n";
		// rst += "last new hang: "+FileUtil.parseSecondsToStringTime(lastNewHangTime)
		// +"("+lastNewHangFaults+" faults)\n";
		total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsHangsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToHangs = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			// rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsHangsNum.get(key);
			int count = 0;
			for(Integer faults:value.keySet()) {
				faultToHangs.computeIfAbsent(faults, k->0);
				faultToHangs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
				// rst += "--"+value.get(faults)+" "+faults+"-faults tests caused hangs. \n";
				// count += value.get(faults);
			}
			// rst += "**"+count+" tests caused hangs in this time window. \n";
			// total += count;
			// rst += "**"+total+" tests caused hangs in total for now. \n";
			// for(Integer i:faultToHangs.keySet()) {
			// 	rst += "**"+faultToHangs.get(i)+" "+i+"-faults tests caused hangs in total for now. \n";
			// }
			// rst += "---------------------------------------------------------------------------------\n";
		}

		for (int i = 0; i < faultToHangs.size(); i++) {
			rst += "--"+faultToHangs.get(i)+" "+i+"-faults tests were executed in total for now. \n";
		}

		rst += "*************************************END*****************************************\n";
		return rst;

	}





	public static String generateBeautifulReport2(String currentTime,
		String testTime, int totalCoverage, int totalBugs,
		int totalHangs, String lastNewCoverage, long totalExecs, 
		long totalNoTriggeres, long totalNewCovTest, 
		HashMap<Integer, Integer> faultToExec, HashMap<Integer, Integer> faultToNewCovs) {

		/*
		 *********************************************************************************
		**************************** Test result statistics *****************************
		**************************** 2023-10-20 17:24:41 PM *****************************
		*********************************************************************************

		+-----------------+---------------------+------------------+---------------------+
		| Tested time     | 1h28m2s             | Covered basic    | 1782                |
		|                 |                     | code blocks      |                     |
		+-----------------+---------------------+------------------+---------------------+
		| Detected bugs   | 1 bugs              | last new         | 1h3m54s             |
		|                 | 0 hang bugs         | coverate         |                     |
		+-----------------+---------------------+------------------+---------------------+
		| Tested fault    |                            25 fault sequences were executed. |
		| sequences       |                        1 fault sequences were not triggered. |
		|                 |         11 fault sequences increased code coverage in total. |
		+-----------------+---------------------+------------------+---------------------+
		|                            19 1-faults fault sequences were executed in total. |
		|                             5 2-faults fault sequences were executed in total. |
		|                             1 3-faults fault sequences were executed in total. |
		+-----------------+---------------------+------------------+---------------------+
		|                   8 1-faults fault sequences increased code coverage in total. |
		|                   2 2-faults fault sequences increased code coverage in total. |
		|                   1 3-faults fault sequences increased code coverage in total. |
		+-----------------+---------------------+------------------+---------------------+

		************************************** END **************************************
		 */

		String testTimeFormat = String.format("%-20s", testTime);
		String totalCoverageFormat = String.format("%-20d", totalCoverage);
		int width = 20;
		String totalBugsFormat = bugFormat(totalBugs, " bugs", width);
		String totalHangsFormat = bugFormat(totalHangs, " hang bugs", width);

		String lastNewCoverageFormat = String.format("%-20s", lastNewCoverage);

		String totalExecsFormat = String.format("%30d", totalExecs);		
		String totalNoTriggeresFormat = String.format("%25d", totalNoTriggeres);
		String totalNewCovTestFormat = String.format("%11d", totalNewCovTest);

		List<Integer> keyListOfFaultToExec = faultToExec.keySet().stream().collect(Collectors.toList());
		//sort the keyListOfFaultToExec
		keyListOfFaultToExec.sort(Comparator.comparingInt(x -> x));


		List<Integer> keyListOfFaultToNewCovs = faultToNewCovs.keySet().stream().collect(Collectors.toList());
		//sort the keyListOfFaultToNewCovs
		keyListOfFaultToNewCovs.sort(Comparator.comparingInt(x -> x));


		String s = "";

		s = s + "*********************************************************************************" + "\n";
        s = s + "**************************** Test result statistics *****************************" + "\n";
        s = s + "**************************** " + currentTime + " *****************************" + "\n";
        s = s + "*********************************************************************************" + "\n\n";

        s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
        s = s + "| Tested time     | "+testTimeFormat + "| Covered basic    | "+totalCoverageFormat+"|" + "\n";
        s = s + "|                 |                     | code blocks      |                     |" + "\n";
        s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
        s = s + "| Detected bugs   | "+totalBugsFormat + "| last new         | "+lastNewCoverageFormat+"|" + "\n";
        s = s + "|                 | "+totalHangsFormat + "| coverate         |                     |" + "\n";
        s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
        s = s + "| Tested fault    |"+totalExecsFormat + " fault sequences were executed. |" + "\n";
        s = s + "| sequences       |"+totalNoTriggeresFormat + " fault sequences were not triggered. |" + "\n";
		s = s + "|                 |"+totalNewCovTestFormat + " fault sequences increased code coverage in total. |" + "\n";
        s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";

		if (keyListOfFaultToExec.size() >= 2) {
			int formatLength = 32;
			for (int i = 1; i < keyListOfFaultToExec.size(); i++) {
				int key = keyListOfFaultToExec.get(i);
				int num = faultToExec.get(key);
				String format = getFixedWidthTwoNumberString(num, key, formatLength);
				s = s + "|"+ format +"-faults fault sequences were executed in total. |" + "\n";
			}
			s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
		}

		if (keyListOfFaultToNewCovs.size() >= 2) {
			int formatLength = 22;
			for (int i = 1; i < keyListOfFaultToNewCovs.size(); i++) {
				int key = keyListOfFaultToNewCovs.get(i);
				int num = faultToNewCovs.get(key);
				String format = getFixedWidthTwoNumberString(num, key, formatLength);
				s = s + "|"+ format +"-faults fault sequences increased code coverage in total. |" + "\n";
			}
			s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
		}

		s = s + "\n";
		s = s + "************************************** END **************************************" + "\n";
		return s;
	}

	public static String generateBeautifulReport() {
		
		SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记
		Date date = new Date();// 获取当前时间
		String currentTime = sdf.format(date);

		String testTime = FileUtil.parseSecondsToStringTime(FuzzInfo.getUsedSeconds());

		int totalCoverage = 0;
		List<Integer> sortedKeys = new ArrayList<Integer>();		
		sortedKeys.addAll(timeToTotalCovs.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		if (!sortedKeys.isEmpty()) {
			int lastKey = sortedKeys.get(sortedKeys.size()-1);
			totalCoverage = timeToTotalCovs.get(lastKey);
		}

		int totalBugs = FuzzInfo.total_bugs;
		int totalHangs = FuzzInfo.total_hangs;

		String lastNewCoverage = FileUtil.parseSecondsToStringTime(lastNewCovTime);

		long totalExecs = FuzzInfo.total_execs;
		totalExecs = totalExecs - Conf.WORKLOADLIST.size();
		if (totalExecs < 0) {
			totalExecs = 0;
		}
		
		long totalNoTriggeres = FuzzInfo.total_nontrigger;


		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsToTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			HashMap<Integer, Integer> value = timeToFaulsToTestsNum.get(key);
			for(Integer faults:value.keySet()) {
				faultToExec.computeIfAbsent(faults, k->0);
				faultToExec.computeIfPresent(faults, (k, v) -> v + value.get(faults));
			}
		}

		long totalNewCovTest = 0;
		// loop faultToExec and sum up the value
		for (Integer key : faultToExec.keySet()) {
			if (key == 0) {
				continue;
			}
			totalNewCovTest += faultToExec.get(key);
		}
		
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsToNewCovTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			HashMap<Integer, Integer> value = timeToFaulsToNewCovTestsNum.get(key);
			for(Integer faults:value.keySet()) {
				faultToNewCovs.computeIfAbsent(faults, k->0);
				faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
			}
		}

		String beautifulBugReport = generateBeautifulReport2(currentTime, testTime, totalCoverage, totalBugs,
				totalHangs, lastNewCoverage, totalExecs,
				totalNoTriggeres, totalNewCovTest,
				faultToExec, faultToNewCovs);

		return beautifulBugReport;

	}

	// public static String generateBeautifulReport() {

	// 	String rst = "************************************REPORT**************************************\n";

	// 	SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间 
    //     sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记  
    //     Date date = new Date();// 获取当前时间 
    //     String currentTime = sdf.format(date);

	// 	String testTime = FileUtil.parseSecondsToStringTime(FuzzInfo.getUsedSeconds());
	// 	String testTimeFormat = String.format("%-20s", testTime);

	// 	int totalCoverage = 0;
	// 	List<Integer> sortedKeys = new ArrayList<Integer>();		
	// 	sortedKeys.addAll(timeToTotalCovs.keySet());
	// 	sortedKeys.sort(Comparator.comparingInt(x -> x));
	// 	if (!sortedKeys.isEmpty()) {
	// 		int lastKey = sortedKeys.get(sortedKeys.size()-1);
	// 		totalCoverage = timeToTotalCovs.get(lastKey);
	// 	}
	// 	String totalCoverageFormat = String.format("%-20d", totalCoverage);

	// 	int width = 20;
	// 	int totalBugs = FuzzInfo.total_bugs;
	// 	String totalBugsFormat = bugFormat(totalBugs, " bugs", width);
	// 	int totalHangs = FuzzInfo.total_hangs;
	// 	String totalHangsFormat = bugFormat(totalHangs, " hang bugs", width);

		

	// 	String lastNewCoverage = FileUtil.parseSecondsToStringTime(lastNewCovTime);
	// 	String lastNewCoverageFormat = String.format("%-20s", lastNewCoverage);

	// 	long totalExecs = FuzzInfo.total_execs;
	// 	String totalExecsFormat = String.format("%30s", totalExecs);		
	// 	long totalNoTriggeres = FuzzInfo.total_nontrigger;
	// 	String totalNoTriggeresFormat = String.format("%26s", totalNoTriggeres);

	// 	sortedKeys.clear();
	// 	sortedKeys.addAll(timeToFaulsToTestsNum.keySet());
	// 	sortedKeys.sort(Comparator.comparingInt(x -> x));
	// 	HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
	// 	for(Integer key:sortedKeys) {
	// 		HashMap<Integer, Integer> value = timeToFaulsToTestsNum.get(key);
	// 		for(Integer faults:value.keySet()) {
	// 			faultToExec.computeIfAbsent(faults, k->0);
	// 			faultToExec.computeIfPresent(faults, (k, v) -> v + value.get(faults));
	// 		}
	// 	}
	// 	List<Integer> keyListOfFaultToExec = new ArrayList<Integer>();
	// 	keyListOfFaultToExec.addAll(sortedKeys);

	// 	int totalNewCovTest = 0;
	// 	// loop faultToExec and sum up the value
	// 	for (Integer key : faultToExec.keySet()) {
	// 		if (key == 0) {
	// 			continue;
	// 		}
	// 		totalNewCovTest += faultToExec.get(key);
	// 	}
	// 	String totalNewCovTestFormat = String.format("%-11d", totalNewCovTest);
		
	// 	sortedKeys.clear();
	// 	sortedKeys.addAll(timeToFaulsToNewCovTestsNum.keySet());
	// 	sortedKeys.sort(Comparator.comparingInt(x -> x));
	// 	HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
	// 	for(Integer key:sortedKeys) {
	// 		HashMap<Integer, Integer> value = timeToFaulsToNewCovTestsNum.get(key);
	// 		for(Integer faults:value.keySet()) {
	// 			faultToNewCovs.computeIfAbsent(faults, k->0);
	// 			faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
	// 		}
	// 	}
	// 	List<Integer> keyListOfFaultToNewCovs = new ArrayList<Integer>();
	// 	keyListOfFaultToNewCovs.addAll(sortedKeys);

	// 	String s = "";

	// 	String t = "*********************************************************************************\n" +
    //             "**************************** Test result statistics ****************************\n" +
    //             "**************************** 2023-10-20 17:24:41 PM ****************************\n" +
    //             "*********************************************************************************\n" +
    //             "\n" +
    //             "+-----------------+---------------------+------------------+---------------------+\n" +
    //             "| Tested time     | 1h28m2s             | Covered basic    | 1702                |\n" +
    //             "|                 |                     | code blocks      |                     |\n" +
    //             "+-----------------+---------------------+------------------+---------------------+\n" +
    //             "| Detected bugs   | 1 bugs              | last new         | 1h3m54s             |\n" +
    //             "|                 | 0 hang bugs         | coverate         |                     |\n" +
    //             "+-----------------+---------------------+------------------+---------------------+\n" +
    //             "| Tested fault    |                            25 fault sequences were executed. |\n" +
    //             "| sequences       |                        1 fault sequences were not triggered. |\n" +
    //             "|                 |         11 fault sequences increased code coverage in total. |\n" +
    //             "+-----------------+--------------------------------------------------------------+\n" +
    //             "|                            19 1-faults fault sequences were executed in total. |\n" +
    //             "|                             5 2-faults fault sequences were executed in total. |\n" +
    //             "|                             1 3-faults fault sequences were executed in total. |\n" +
    //             "+--------------------------------------------------------------------------------+\n" +
    //             "|                   8 1-faults fault sequences increased code coverage in total. |\n" +
    //             "|                   2 2-faults fault sequences increased code coverage in total. |\n" +
    //             "|                   1 3-faults fault sequences increased code coverage in total. |\n" +
    //             "+--------------------------------------------------------------------------------+\n" +
    //             "\n" +
    //             "************************************** END **************************************";


	// 	s = s + "*********************************************************************************" + "\n";
    //     s = s + "**************************** Test result statistics ****************************" + "\n";
    //     s = s + "****************************" + currentTime + "****************************" + "\n";
    //     s = s + "*********************************************************************************" + "\n\n";

    //     s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
    //     s = s + "| Tested time     | "+testTimeFormat + "| Covered basic    | "+totalCoverageFormat+"|" + "\n";
    //     s = s + "|                 |                     | blocks           |                     |" + "\n";
    //     s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
    //     s = s + "| Detected bugs   | "+totalBugsFormat + "| last new         | "+lastNewCoverageFormat+"|" + "\n";
    //     s = s + "|                 | "+totalHangsFormat + "| coverate         |                     |" + "\n";
    //     s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
    //     s = s + "| Tested fault    |"+totalExecsFormat + " fault sequences |" + "\n";
    //     s = s + "| sequences       |"+totalNoTriggeresFormat + " no triggered fault sequences |" + "\n";
	// 	s = s + "|                 |"+totalNewCovTestFormat + " no triggered fault sequences |" + "\n";
    //     s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";

    //     s = s + "| Test count      |          19 1-faults fault sequences were executed in total. |" + "\n";
    //     s = s + "|                 |           5 2-faults fault sequences were executed in total. |" + "\n";
    //     s = s + "|                 |           1 3-faults fault sequences were executed in total. |" + "\n";
    //     s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";


	// 	if (keyListOfFaultToExec.size() >= 2) {
	// 		// int firstKey = keyListOfFaultToExec.get(1);
	// 		// int firstNum = faultToExec.get(firstKey);
	// 		int formatLength = 28;
	// 		// String firstFormat = getFixedWidthTwoNumberString(firstNum, firstKey, formatLength);
	// 		// s = s + "| Test count      |"+ firstFormat +"-faults tests were executed in total. |" + "\n";
	// 		for (int i = 1; i < keyListOfFaultToExec.size(); i++) {
	// 			int key = keyListOfFaultToExec.get(i);
	// 			int num = faultToExec.get(key);
	// 			String format = getFixedWidthTwoNumberString(num, key, formatLength);
	// 			s = s + "|"+ format +"-faults tests increased code coverage in total. |" + "\n";
	// 		}
	// 		s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
	// 	}

	// 	if (keyListOfFaultToNewCovs.size() >= 2) {
	// 		int formatLength = 22;
	// 		for (int i = 1; i < keyListOfFaultToNewCovs.size(); i++) {
	// 			int key = keyListOfFaultToNewCovs.get(i);
	// 			int num = faultToNewCovs.get(key);
	// 			String format = getFixedWidthTwoNumberString(num, key, formatLength);
	// 			s = s + "|"+ format +"-faults fault sequences covered new codes in total. |" + "\n";
	// 		}
	// 	}

    //     System.out.println(s);
        
	// 	return s;
	// }

	public static String bugFormat(int num, String bugs, int totalLength) {
        String formattedBugString = String.format("%d%s", num, bugs);
        int paddingLength = totalLength - formattedBugString.length();
        for (int i = 0; i < paddingLength; i++) {
            formattedBugString += " ";
        }
		return formattedBugString;
	}

	public static String getFixedWidthTwoNumberString(int number1, int number2, int totalLength) {
        int numberLength1 = String.valueOf(number1).length();
        int numberLength2 = String.valueOf(number2).length();

        // 计算需要的空格数
        int spaceLength = totalLength - numberLength1 - numberLength2 - 1;

        // 使用 String.format 格式化字符串
        String formattedString = String.format("%1$-" + spaceLength + "s%2$d %3$d", "", number1, number2);

        return formattedString;
    }

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
		rst += "last new coverate: "+FileUtil.parseSecondsToStringTime(lastNewCovTime)
		+"("+lastNewCovFaults+" faults)\n";
		List<Integer> sortedKeys = new ArrayList<Integer>();
		sortedKeys.addAll(timeToTotalCovs.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		for(Integer key:sortedKeys) {
			rst += "for "+key+"th "+reportWindow+" minutes, the total coverage is "+timeToTotalCovs.get(key)+"\n";
		}
		int total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsToTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
		if(!sortedKeys.isEmpty()) {
			rst += "*****************************TEST COUNT******************************************\n";
		}
		for(Integer key:sortedKeys) {
			rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsToTestsNum.get(key);
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
		sortedKeys.addAll(timeToFaulsToNewCovTestsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
		if(!sortedKeys.isEmpty()) {
			rst += "******************************NEW COV TEST COUNT*********************************\n";
		}
		for(Integer key:sortedKeys) {
			rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsToNewCovTestsNum.get(key);
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
		rst += "last new bug: "+FileUtil.parseSecondsToStringTime(lastNewBugTime)
		+"("+lastNewBugFaults+" faults)\n";
		total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsBugsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToBugs = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsBugsNum.get(key);
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
		rst += "last new hang: "+FileUtil.parseSecondsToStringTime(lastNewHangTime)
		+"("+lastNewHangFaults+" faults)\n";
		total = 0;
		sortedKeys.clear();
		sortedKeys.addAll(timeToFaulsHangsNum.keySet());
		sortedKeys.sort(Comparator.comparingInt(x -> x));
		HashMap<Integer, Integer> faultToHangs = new HashMap<Integer, Integer>();
		for(Integer key:sortedKeys) {
			rst += "for "+key+"th "+reportWindow+" minutes: \n";
			HashMap<Integer, Integer> value = timeToFaulsHangsNum.get(key);
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

	public static String toJSONString() {
		String result = "transform FuzzInfo to JSONString fail";
		// FuzzInfo fuzzInfo = new FuzzInfo();
		FuzzInfoRecord record = new FuzzInfoRecord();
		result = JSONObject.toJSONString(record);
		return result;
	}

    public static void updateTimeToFaulsHangsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToHangs = timeToFaulsHangsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToHangs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToHangs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsToTestsNum(QueueEntry q) {
    	FuzzInfo.updateTimeToFaulsToTestsNum(q.faultSeq);
    }

    public static void updateTimeToFaulsToTestsNum(FaultSequence fs) {
    	HashMap<Integer, Integer> faultsToTests = timeToFaulsToTestsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToTests.computeIfAbsent(fs.seq.size(), key -> 0);
    	faultsToTests.computeIfPresent(fs.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsToNewCovTestsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToNewCovTests = timeToFaulsToNewCovTestsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToNewCovTests.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToNewCovTests.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }

    public static void updateTimeToFaulsBugsNum(QueueEntry q) {
    	HashMap<Integer, Integer> faultsToBugs = timeToFaulsBugsNum.computeIfAbsent((int) (getUsedSeconds()/(reportWindow*60)), k -> new HashMap<Integer, Integer>());
    	faultsToBugs.computeIfAbsent(q.faultSeq.seq.size(), key -> 0);
    	faultsToBugs.computeIfPresent(q.faultSeq.seq.size(), (key, value) -> value + 1);
    }
}
