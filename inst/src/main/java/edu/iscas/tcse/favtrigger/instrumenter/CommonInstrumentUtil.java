package edu.iscas.tcse.favtrigger.instrumenter;

import java.util.List;

import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

public class CommonInstrumentUtil {

    public static String combineIpWithMsgid(String ip, int msgid) {
        return FAVPathType.FAVMSG.toString()+":"+ip+"&"+msgid;
    }

    public static String combineIpWithMsgidForRead(String ip, int msgid) {
        return FAVPathType.FAVMSG.toString()+":READ"+ip+"&"+msgid;
    }

    public static String combineIpWithLogicClockMsg(String ip, String logicClockMsg) {
        return FAVPathType.FAVMSG.toString()+":"+ip+"&"+logicClockMsg;
    }

    public static String combineIpWithLogicClockMsgForRead(String ip, String logicClockMsg) {
        return FAVPathType.FAVMSG.toString()+":READ"+ip+"&"+logicClockMsg;
    }

    public static byte[] transformStrToByteArr(String s) {
        return s.getBytes();
    }

    public static String transformByteArrToStr(byte[] bArr) {
        String result = new String(bArr);
        return result;
    }

    // input: FAVMSG:172.42.0.1&3#2
    // output: 172.42.0.1
    public static String getRemoteAddrFromSource(String source) {
        //System.out.println("!!!GY getRemoteAddrFromSource:"+source);
        if(source.equals(FAVPathType.FAVMSG.toString()+":")) {//do not have the remote server info
            return "";
        } else {
            return source.substring(source.indexOf(":")+1, source.lastIndexOf("&"));
        }
    }

    // input: 172.42.0.1&3#2
    // output: 172.42.0.1
    public static String getRemoteAddrFromSourceLogicClockMsg(String source) {
        if(source.equals(FAVPathType.FAVMSG.toString()+":")) {
            return "";
        }
        return source.substring(0, source.lastIndexOf("&"));
    }

    // input: FAVMSG:172.42.0.1&3#2
    // output: 3#2
    // input: 172.42.0.1&3#2
    // output: 3#2
    public static String getMsgIdFromSourceLogicClockMsg(String source) {
        if(source.equals(FAVPathType.FAVMSG.toString()+":")) {
            return "";
        }
        return source.substring(source.lastIndexOf("&")+1, source.length());
    }
    
    public static Object getLinkSourceFromMsg(List list) {
        try {
            return list.get(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("!!!FAVTrigger get rpc message id from message wrong:"+e);
            e.printStackTrace();
        }
        //maybe the client side was not run with the USE_FAV and FOR_YARN or
        //YARN_RPC configuration
        return FAVPathType.FAVMSG.toString()+":";
    }
}
