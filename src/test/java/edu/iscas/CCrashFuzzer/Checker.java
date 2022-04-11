package edu.iscas.CCrashFuzzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Checker {

    static int COVERAGE = 0;
    static int TEST_COUNT = 1;
    static int BUG_COUNT = 2;
    static int NEW_COV_TEST_COUNT = 3;
    static int HANG_COUNT = 4;

    static int max_time = 162;

    Map<String, int[][]> a = new HashMap<>();

    int state = -1;

    public void check(String file) throws IOException {

        int index = 0;
        int total = 0;
        int zeroOrOne = 0;
        FileInputStream in = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            if (str.contains("*COVERAGE*")) {
                state = COVERAGE;
                a.put("COVERAGE", new int[max_time][2]);
            }

            if (state == COVERAGE) {
                if (str.contains("for") && str.contains("30 minutes,")) {
                    int nIndex = Integer.parseInt(str.substring(4).split("th")[0]);
                    int num = Integer.parseInt(str.substring(str.indexOf("is ") + 3));
                    a.get("COVERAGE")[nIndex][0] = num;
                }
            }

            if (str.contains("*TEST COUNT*")) {
                state = TEST_COUNT;
                a.put("TEST_COUNT", new int[max_time][2]);
            }

            if (state == TEST_COUNT) {
                if (str.contains("for") && str.contains("30 minutes:")) {
                    index = Integer.parseInt(str.substring(4).split("th")[0]);
                }
                if (str.contains("were executed in total for now") && (!str.contains("-faults"))) {
                    total = Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("were executed in total for now") && str.contains(" 0-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("were executed in total for now") && str.contains(" 1-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("----------------------")) {
                    a.get("TEST_COUNT")[index][0] = total;
                    a.get("TEST_COUNT")[index][1] = total - zeroOrOne;
                    total = 0;
                    zeroOrOne = 0;
                }
            }

            if (str.contains("**NEW COV TEST COUNT**")) {
                state = NEW_COV_TEST_COUNT;
                a.put("NEW_COV_TEST_COUNT", new int[max_time][2]);
            }

            if (state == NEW_COV_TEST_COUNT) {
                if (str.contains("for") && str.contains("30 minutes:")) {
                    index = Integer.parseInt(str.substring(4).split("th")[0]);
                }
                if (str.contains("tests resulted in new coverages in total for now") && (!str.contains("-faults"))) {
                    total = Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests resulted in new coverages in total for now") && str.contains(" 0-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests resulted in new coverages in total for now") && str.contains(" 1-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("----------------------")) {
                    a.get("NEW_COV_TEST_COUNT")[index][0] = total;
                    a.get("NEW_COV_TEST_COUNT")[index][1] = total - zeroOrOne;
                    total = 0;
                    zeroOrOne = 0;
                }
            }

            if (str.contains("**BUG COUNT**")) {
                state = BUG_COUNT;
                a.put("BUG_COUNT", new int[max_time][2]);
            }

            if (state == BUG_COUNT) {
                if (str.contains("for") && str.contains("30 minutes:")) {
                    index = Integer.parseInt(str.substring(4).split("th")[0]);
                }
                if (str.contains("tests caused bugs in total for now") && (!str.contains("-faults"))) {
                    total = Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests caused bugs in total for now") && str.contains(" 0-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests caused bugs in total for now") && str.contains(" 1-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("----------------------")) {
                    a.get("BUG_COUNT")[index][0] = total;
                    a.get("BUG_COUNT")[index][1] = total - zeroOrOne;
                    total = 0;
                    zeroOrOne = 0;
                }
            }

            if (str.contains("**HANG COUNT**")) {
                state = HANG_COUNT;
                a.put("HANG_COUNT", new int[max_time][2]);
            }

            if (state == HANG_COUNT) {
                if (str.contains("for") && str.contains("30 minutes:")) {
                    index = Integer.parseInt(str.substring(4).split("th")[0]);
                }
                if (str.contains("tests caused hangs in total for now") && (!str.contains("-faults"))) {
                    total = Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests caused hangs in total for now") && str.contains(" 0-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("tests caused hangs in total for now") && str.contains(" 1-faults ")) {
                    zeroOrOne += Integer.parseInt(str.split(" ")[0].substring(2));
                }
                if (str.contains("----------------------")) {
                    a.get("HANG_COUNT")[index][0] = total;
                    a.get("HANG_COUNT")[index][1] = total - zeroOrOne;
                    total = 0;
                    zeroOrOne = 0;
                }
            }


        }
        //close
        in.close();
        bufferedReader.close();


        for (int[][] arr : a.values()) {
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i + 1][0] == 0) {
                    arr[i + 1][0] = arr[i][0];
                }
                if (arr[i + 1][1] == 0) {
                    arr[i + 1][1] = arr[i][1];
                }
            }
        }

        int[][] cov = a.get("COVERAGE");
//        for (int i = 0; i < cov.length; i++) {
//            System.out.println("" + i + ", " + cov[i][0]);
//        }

        for (int i = 0; i < 96; i++) {
//            System.out.println(cov[i][0]);
        }

        int[][] res = a.get("HANG_COUNT");
//        for (int i = 0; i < res.length; i++) {
//            System.out.println("" + i + ", " + res[i][0] + ", " +res[i][1]);
//        }

        for (int i = 0; i < 96; i++) {
//            System.out.println(res[i][0]);
        }

//        for (int i = 0; i < 96; i++) {
//            System.out.println(res[i][1]);
//        }
        
        int[][] count = a.get("TEST_COUNT");
//      for (int i = 0; i < res.length; i++) {
//          System.out.println("" + i + ", " + res[i][0] + ", " +res[i][1]);
//      }

      for (int i = 0; i < 96; i++) {
//          System.out.println(count[i][0]);
      }
      
      int[][] new_cov_count = a.get("NEW_COV_TEST_COUNT");
//    for (int i = 0; i < res.length; i++) {
//        System.out.println("" + i + ", " + res[i][0] + ", " +res[i][1]);
//    }

    for (int i = 0; i < 96; i++) {
//        System.out.println(new_cov_count[i][0]);
    }
    
    int[][] bug_count = a.get("BUG_COUNT");
//  for (int i = 0; i < res.length; i++) {
//      System.out.println("" + i + ", " + res[i][0] + ", " +res[i][1]);
//  }

  for (int i = 0; i < 96; i++) {
      System.out.println(bug_count[i][0]);
  }

    }

}
