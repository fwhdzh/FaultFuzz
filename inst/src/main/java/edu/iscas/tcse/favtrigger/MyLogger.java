package edu.iscas.tcse.favtrigger;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;


public class MyLogger {

    public static boolean useDebug = false;

	public static String log(String s) {
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer] - INFO - "+s;
        System.out.println(rst);
        return rst;
	}

    public void test() {
        AflCli c = new AflCli();
    }

    public static String log(Class c, String s) {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer][" + c.getName() + "] - INFO - "+s;
        System.out.println(rst);
        return rst;
    }

    public static String debug(String s) {
        if (!useDebug) {
            return "";
        }
	    Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer] - INFO - "+s;
        System.out.println(rst);
        return rst;
	}

    public static String debug(Class c, String s) {
        if (!useDebug) {
            return "";
        }
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer][" + c.getName() + "] - INFO - "+s;
        System.out.println(rst);
        return rst;
    }
}
