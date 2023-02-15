package edu.iscas.tcse.favtrigger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
	public static String log(String s) {
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer] - INFO - "+s;
        System.out.println(rst);
        return rst;
	}
}
