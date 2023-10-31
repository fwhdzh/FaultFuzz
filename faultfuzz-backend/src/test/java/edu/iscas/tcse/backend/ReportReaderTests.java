package edu.iscas.tcse.backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ReportReaderTests {
    @Disabled
    @Test
    void testPrintReport() throws IOException, InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                ReportReader rr = new ReportReader();
                try {
                    rr.readAndPrintReport100Times();
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        t.start();
        File f = new File("E:\\javafile\\faultfuzz-backend\\src\\test\\java\\edu\\iscas\\tcse\\backend\\report.txt");
        if (!f.exists()) {
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        for (int i = 0; i < 100; i++) {
            // System.out.println("write print report: " + i);
            String s = "print report: " + i + "\n";
            fos.write(s.getBytes());
            Thread.sleep(1000);
        }
        fos.close();
        
    }

    @Disabled
    @Test
    public void fwhTest() {
        RunCommand.run("/data/fengwenhan/code/faultfuzz/faultfuzz-backend/scripts/clear-master-process.sh");
    }
}
