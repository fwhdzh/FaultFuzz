package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Stat {
	public static final String bug = "IS_BUG";
	public static final String newCov = "HAS_NEW_COV";
	public String saveInitStat() {
		return null;
	}
	//return file name that stores the test case
	public String saveTestCase() {
		return null;
	}
	public static void markBug(String dir) {
		File f = new File(dir+Stat.bug);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void markNewCoverage(String dir, int newCovs) {
		File f = new File(dir+Stat.newCov);

		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(Integer.toString(newCovs).getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String log(String s) {
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [CrashFuzz] - INFO - "+s;
        System.out.println(rst);
        return rst;
	}
	public static String warn(String s) {
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [CrashFuzz] - WARN - "+s;
        System.out.println(rst);
        return rst;
	}
	public static void deleteEmptyFile(File file) {
		if(file.isDirectory()) {
			if(file.list().length == 0) {
				file.delete();
			} else {
				File[] childFiles = file.listFiles();
				for(File f:childFiles) {
					deleteEmptyFile(f);
				}
				if(file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			if(file.length() == 0) {
				file.delete();
			}
		}
    }
}
