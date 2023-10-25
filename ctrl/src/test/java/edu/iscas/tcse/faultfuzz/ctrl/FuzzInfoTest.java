package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.HashMap;

import org.junit.Test;

public class FuzzInfoTest {
    @Test
    public void testGenerateBeautifulReport() {
        String myString = "20h28m2s";
        int totalLength = 20;

        // String formattedString = String.format("%-" + totalLength + "s", myString);
        String formattedString = String.format("%-20s", myString);
        System.out.println("Formatted string: '" + formattedString + "'");

        int totalCoverage = 23432;
		String totalCoverageFormat = String.format("%-20d", totalCoverage);
        System.out.println("Formatted string: '" + totalCoverageFormat + "'");

        // int num = 1; // 你任意填充的数字
        // String bugs = " bugs";

        // String formattedBugString = String.format("%-16d%s", num, bugs);
        // System.out.println(formattedBugString);

        int num = 1; // 你任意填充的数字
        String bugs = " bugs";

        String formattedBugString = String.format("%d%s", num, bugs);
        int paddingLength = totalLength - formattedBugString.length();
        for (int i = 0; i < paddingLength; i++) {
            formattedBugString += " ";
        }
        System.out.println("Formatted string: '" + formattedBugString + "'");

        int number = 123; // 你的数字
        String paddedNumber = String.format("%10s", number);
        System.out.println("Formatted string: '" + paddedNumber + "'");

        long totalExecs = 323;
        String totalExecsFormat = String.format("%30d", totalExecs);	
        System.out.println("Formatted string: '" + totalExecsFormat + "'");
        long totalNoTriggeres = 2324;	
		String totalNoTriggeresFormat = String.format("%26d", totalNoTriggeres);
        System.out.println("Formatted string: '" + totalNoTriggeresFormat + "'");
    }

    

    @Test
    public void printToString() {

        String formattedString = FuzzInfo.getFixedWidthTwoNumberString(435345, 123, 24);

        System.out.println("生成的字符串为: " + formattedString);
        System.out.println("占位符用于比较: " + "                    13 1");
        // return formattedString;
    }


    

    @Test
    public void testGenerateBeautifulReport2() {
        String t = "*********************************************************************************\n" +
                "**************************** Test result statistics ****************************\n" +
                "**************************** 2023-10-20 17:24:41 PM ****************************\n" +
                "*********************************************************************************\n" +
                "\n" +
                "+-----------------+---------------------+------------------+---------------------+\n" +
                "| Tested time     | 1h28m2s             | Covered basic    | 1702                |\n" +
                "|                 |                     | code blocks      |                     |\n" +
                "+-----------------+---------------------+------------------+---------------------+\n" +
                "| Detected bugs   | 1 bugs              | last new         | 1h3m54s             |\n" +
                "|                 | 0 hang bugs         | coverate         |                     |\n" +
                "+-----------------+---------------------+------------------+---------------------+\n" +
                "| Tested fault    |                            25 fault sequences were executed. |\n" +
                "| sequences       |                        1 fault sequences were not triggered. |\n" +
                "|                 |         11 fault sequences increased code coverage in total. |\n" +
                "+-----------------+--------------------------------------------------------------+\n" +
                "|                            19 1-faults fault sequences were executed in total. |\n" +
                "|                             5 2-faults fault sequences were executed in total. |\n" +
                "|                             1 3-faults fault sequences were executed in total. |\n" +
                "+--------------------------------------------------------------------------------+\n" +
                "|                   8 1-faults fault sequences increased code coverage in total. |\n" +
                "|                   2 2-faults fault sequences increased code coverage in total. |\n" +
                "|                   1 3-faults fault sequences increased code coverage in total. |\n" +
                "+--------------------------------------------------------------------------------+\n" +
                "\n" +
                "************************************** END **************************************";
        String currentTime = "2023-10-20 17:24:41 PM";
        String testTime = "1h28m2s";
        int totalCoverage = 1782;
        int totalBugs = 1;
        int totalHangs = 0;
        String lastNewCoverage = "1h3m54s";
        long totalExecs = 25;
        long totalNoTriggeres = 1;
        long totalNewCovTest = 11;
        HashMap<Integer, Integer> faultToExec = new HashMap<>();
        faultToExec.put(0, 2);
        faultToExec.put(1, 19);
        faultToExec.put(2, 5);
        faultToExec.put(3, 1);
        HashMap<Integer, Integer> faultToNewCovs = new HashMap<>();
        faultToNewCovs.put(0, 2);
        faultToNewCovs.put(1, 8);
        faultToNewCovs.put(2, 2);
        faultToNewCovs.put(3, 1);
        String result = FuzzInfo.generateBeautifulReport2(currentTime, testTime, totalCoverage, totalBugs, totalHangs, lastNewCoverage, totalExecs, totalNoTriggeres, totalNewCovTest, faultToExec, faultToNewCovs);
        System.out.println(result);
    }
}
