package edu.iscas.CCrashFuzzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {

        Checker checker = new Checker();

        String zkFile = "E:\\work3\\evaluation\\zk-CrashFuzz-TEST_REPORT";
        String hBaseFile = "E:\\work3\\evaluation\\hbase-BruteForce-TEST_REPORT";
        String hBaseBRFile = "E:\\work3\\evaluation\\hbase-CovBrute-TEST_REPORT";
        String hdfsCFFile = "E:\\work3\\evaluation\\hdfs-random-TEST_REPORT";
        checker.check(hdfsCFFile);
    }
}
