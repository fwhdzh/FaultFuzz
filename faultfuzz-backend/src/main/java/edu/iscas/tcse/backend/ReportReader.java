package edu.iscas.tcse.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReportReader {

    static class BugReportPostParams {
        String ctrlJarPath;
        String ctrlPropertiesPath;

        public String getCtrlJarPath() {
            return ctrlJarPath;
        }
        public void setCtrlJarPath(String ctrlJarPath) {
            this.ctrlJarPath = ctrlJarPath;
        }
        public String getCtrlPropertiesPath() {
            return ctrlPropertiesPath;
        }
        public void setCtrlPropertiesPath(String ctrlPropertiesPath) {
            this.ctrlPropertiesPath = ctrlPropertiesPath;
        }
    }

    static class BugReportGetParams {
        String bugReportLocation;

        public String getBugReportLocation() {
            return bugReportLocation;
        }
        public void setBugReportLocation(String bugReportLocation) {
            this.bugReportLocation = bugReportLocation;
        }
    }

    public String readReport(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            int counter = 0;
            while (!f.exists() && counter < 10) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
        }
        if (!f.exists()) {
            throw new FileNotFoundException("report file not found");
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String result = "";
        String line;
        while ((line = br.readLine()) != null) {
            result = result + line + "\n";
        }
        br.close();
        return result;
    }

    public void readAndPrintReport100Times() throws IOException, InterruptedException {
        for (int i = 0; i < 100; i++) {
            String s = readReport("E:\\javafile\\faultfuzz-backend\\src\\test\\java\\edu\\iscas\\tcse\\backend\\report.txt");
            System.out.println(s);
            Thread.sleep(1000);
        }
    }
}
