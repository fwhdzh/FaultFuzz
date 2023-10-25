package edu.iscas.tcse.ZKCases;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	public static String log(String testName, String s) {
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" ["+testName+"] - INFO - "+s;
        System.out.println(rst);
        return rst;
	}
}
