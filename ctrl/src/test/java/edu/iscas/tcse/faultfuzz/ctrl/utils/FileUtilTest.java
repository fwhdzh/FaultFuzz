package edu.iscas.tcse.faultfuzz.ctrl.utils;

import org.junit.Test;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;

public class FileUtilTest {
    @Test
    public void testLoadcurrentFaultPoint() {
        FaultSequence fs = FileUtil.loadcurrentFaultPoint("/data/fengwenhan/data/faultfuzz_replay_test/2_1f/faultUnderTest");
        System.out.println(fs);
    }

    // public static void renameFiles(String directoryPath) {
    //     File directory = new File(directoryPath);
    //     if (directory.exists() && directory.isDirectory()) {
    //         File[] files = directory.listFiles();
    //         if (files != null) {
    //             for (File file : files) {
    //                 if (file.isDirectory()) {
    //                     renameFiles(file.getAbsolutePath()); // 递归调用以处理子目录
    //                 } else {
    //                     if (file.getName().equals("zk363curFault")) {
    //                         String parentPath = file.getParent();
    //                         File newFile = new File(parentPath + File.separator + "faultUnderTest");
    //                         if (file.renameTo(newFile)) {
    //                             System.out.println("File renamed successfully: " + file.getAbsolutePath());
    //                         } else {
    //                             System.out.println("Failed to rename file: " + file.getAbsolutePath());
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     } else {
    //         System.out.println("Invalid directory path");
    //     }
    // }

    // @Test
    // public void rename() {
    //     renameFiles("/data/fengwenhan/data/faultfuzz_recovery_test");
    // }
}
