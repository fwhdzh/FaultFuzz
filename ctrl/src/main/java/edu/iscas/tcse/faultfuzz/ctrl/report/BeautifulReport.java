package edu.iscas.tcse.faultfuzz.ctrl.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.FuzzInfo;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class BeautifulReport {

    public static String generateBeautifulReport(String currentTime,
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
    	String totalBugsFormat = BeautifulReport.bugFormat(totalBugs, " bugs", width);
    	String totalHangsFormat = BeautifulReport.bugFormat(totalHangs, " hang bugs", width);
    
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
    			String format = BeautifulReport.getFixedWidthTwoNumberString(num, key, formatLength);
    			s = s + "|"+ format +"-faults fault sequences were executed in total. |" + "\n";
    		}
    		s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
    	}
    
    	if (keyListOfFaultToNewCovs.size() >= 2) {
    		int formatLength = 22;
    		for (int i = 1; i < keyListOfFaultToNewCovs.size(); i++) {
    			int key = keyListOfFaultToNewCovs.get(i);
    			int num = faultToNewCovs.get(key);
    			String format = BeautifulReport.getFixedWidthTwoNumberString(num, key, formatLength);
    			s = s + "|"+ format +"-faults fault sequences increased code coverage in total. |" + "\n";
    		}
    		s = s + "+-----------------+---------------------+------------------+---------------------+" + "\n";
    	}
    
    	s = s + "\n";
    	s = s + "************************************** END **************************************" + "\n";
    	return s;
    }

    public static String bugFormat(int num, String bugs, int totalLength) {
        String formattedBugString = String.format("%d%s", num, bugs);
        int paddingLength = totalLength - formattedBugString.length();
        for (int i = 0; i < paddingLength; i++) {
            formattedBugString += " ";
        }
    	return formattedBugString;
    }

    public static String generateBeautifulReportWithFuzzInfo() {
    	
    	SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
    	sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记
    	Date date = new Date();// 获取当前时间
    	String currentTime = sdf.format(date);
    
    	String testTime = FileUtil.parseSecondsToStringTime(FuzzInfo.getUsedSeconds());
    
    	int totalCoverage = 0;
    	List<Integer> sortedKeys = new ArrayList<Integer>();		
    	sortedKeys.addAll(FuzzInfo.timeToTotalCovs.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	if (!sortedKeys.isEmpty()) {
    		int lastKey = sortedKeys.get(sortedKeys.size()-1);
    		totalCoverage = FuzzInfo.timeToTotalCovs.get(lastKey);
    	}
    
    	int totalBugs = FuzzInfo.total_bugs;
    	int totalHangs = FuzzInfo.total_hangs;
    
    	String lastNewCoverage = FileUtil.parseSecondsToStringTime(FuzzInfo.lastNewCovTime);
    
    	long totalExecs = FuzzInfo.total_execs;
    	totalExecs = totalExecs - Conf.WORKLOADLIST.size();
    	if (totalExecs < 0) {
    		totalExecs = 0;
    	}
    	
    	long totalNoTriggeres = FuzzInfo.total_nontrigger;
    
    
    	sortedKeys.clear();
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToExec = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToTestsNum.get(key);
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
    	sortedKeys.addAll(FuzzInfo.timeToFaulsToNewCovTestsNum.keySet());
    	sortedKeys.sort(Comparator.comparingInt(x -> x));
    	HashMap<Integer, Integer> faultToNewCovs = new HashMap<Integer, Integer>();
    	for(Integer key:sortedKeys) {
    		HashMap<Integer, Integer> value = FuzzInfo.timeToFaulsToNewCovTestsNum.get(key);
    		for(Integer faults:value.keySet()) {
    			faultToNewCovs.computeIfAbsent(faults, k->0);
    			faultToNewCovs.computeIfPresent(faults, (k, v) -> v + value.get(faults));
    		}
    	}
    
    	String beautifulBugReport = generateBeautifulReport(currentTime, testTime, totalCoverage, totalBugs,
    			totalHangs, lastNewCoverage, totalExecs,
    			totalNoTriggeres, totalNewCovTest,
    			faultToExec, faultToNewCovs);
    
    	return beautifulBugReport;
    
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
    
}
