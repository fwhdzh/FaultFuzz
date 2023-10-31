package edu.iscas.tcse.favtrigger.tracing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.TaintedIntWithObjTag;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPos;
// import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.iscas.tcse.favtrigger.MyLogger;
//import edu.iscas.tcse.favtrigger.instrumenter.SysTime;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;

public class RecordTaint {
	//for test recording ASTORE, delete in the future
	public static void recordTaint(Taint t) throws IOException {
		System.out.println("ASTORE RECORD TAINT");
		System.out.println("ASTORE RECORD TAINT: "+t.toString());
	}

	private static String bytes2Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(HEXES[(b >> 4) & 0x0F]);
            sb.append(HEXES[b & 0x0F]);
        }

        return sb.toString();
    }

	public static long getTimestamp() {
	    //System.load("/home/gaoyu/FAVD/FAVTrigger/systime/systime.so");
		//System.loadLibrary("systime.so");
		if(Configuration.USE_FAULT_FUZZ) {
			// System.load(Configuration.FAV_HOME+"/systime/systime.so");
	        // SysTime systime = new SysTime();
            // long timestamp = systime.rdtscp();
			long timestamp = System.currentTimeMillis();
            return timestamp;
		} else {
			return Long.MIN_VALUE;
		}
	}

	public static Random rand = null;

	synchronized public static int getMsgID() {
		if(Configuration.USE_MSGID || Configuration.YARN_RPC || Configuration.FOR_YARN
		        || Configuration.FOR_MR || Configuration.MR_RPC || Configuration.FOR_HDFS
				|| Configuration.FOR_HBASE || Configuration.HDFS_RPC || Configuration.HBASE_RPC
				|| Configuration.FOR_ZK ) {
			int temp = -1;
            // Random rand = new Random();
			// synchronized (rand) {
				if (rand == null) {
					rand = new Random(Configuration.TAINT_MSG_RAND_SEED);
					
				}
				temp = rand.nextInt(Integer.MAX_VALUE);//exclusive Max_value
			// }
	        return temp;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public static int totalMsgCount = 0;
	// public static int[][] msgCountArr = new int[5][5];
	public static Map<String, Integer> msgCountMap = new HashMap<>();

	synchronized public static String getLogicClockMsgStr(String connectIP) {
		String result = "";
		totalMsgCount++;
		msgCountMap.computeIfAbsent(connectIP, key -> 0);
		msgCountMap.computeIfPresent(connectIP, (key, value) -> value+1 );
		result = result + totalMsgCount + "#" + msgCountMap.get(connectIP);
		return result;
	}

	private static final char[] HEXES = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

	public static String getRecordPath() {
		// MyLogger.log("getRecordPath is invoked");
	    String path = Configuration.FAV_RECORD_PATH + "/" +FAVTaint.getIP().replace("/", "_")+"-"+FAVTaint.getProcessID()+"/"+Thread.currentThread().getId();
        //String path = Configuration.FAV_RECORD_PATH+"tmp";
	    return path;
	}

	// public static String getRecordPath(String ip) {
	// 	// MyLogger.log("getRecordPath is invoked");
	//     String path = Configuration.FAV_RECORD_PATH+ip+FAVTaint.getProcessID()+"/"+Thread.currentThread().getId();
    //     //String path = Configuration.FAV_RECORD_PATH+"tmp";
	//     return path;
	// }

	public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    public static byte[] intToByteArray(int a) {
        return new byte[] {
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),
            (byte) ((a >> 8) & 0xFF),
            (byte) (a & 0xFF)
        };
    }

    public static ByteBuffer newByteBufferWithMsgID(int msgID, ByteBuffer old, String desAddress) {
        if(old == null) {
            return null;
        }
        int pos = old.position();
        int lim = old.limit();
        assert (pos <= lim);
        if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
            byte[] msgIDBytes = intToByteArray(msgID);
            int rem = (pos <= lim ? lim - pos : 0)*5;
            //ByteBuffer bb = sun.nio.ch.Util.getTemporaryDirectBuffer(rem);
            ByteBuffer bb = ByteBuffer.allocate(rem);
            while(old.hasRemaining()) {
                bb.put(old.get());
                bb.put(msgIDBytes);
            }
            bb.flip();
            // Do not update src until we see how many bytes were written
            old.position(pos);
            //System.out.println("!!!!!!!!!!!FAVTrigger: prepare wrapped "+(pos <= lim ? lim - pos : 0)+":"+rem+" bytebuffer with msgid:"+msgID+", to "+desAddress);
            return bb;
        } else {
            //System.out.println("!!!!!!!!!!!FAVTrigger: prepare original bytebuffer without msgid:"+old==null);
            return old;
        }
    }

    public static TaintedIntWithObjTag updateWriteByteBufferResult(TaintedIntWithObjTag wrapBufferWritten, int newPos, ByteBuffer old) {
        TaintedIntWithObjTag rst = new TaintedIntWithObjTag(wrapBufferWritten.taint, wrapBufferWritten.val);
        if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
            if(wrapBufferWritten.val > 0) {
                int pos = old.position();
                int currentWritten = newPos/5;
                int lastWritten = (newPos - wrapBufferWritten.val)/5;
                old.position(currentWritten-lastWritten + pos);
                rst.val = old.position() - pos;
            }
        }
        return rst;
    }

    public static ByteBuffer newByteBufferWaitMsgID(ByteBuffer old) {
        if(old == null) {
            return null;
        }
        int pos = old.position();
        int lim = old.limit();
        if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
            int rem = (pos <= lim ? lim - pos : 0)*5;
            //ByteBuffer bb = sun.nio.ch.Util.getTemporaryDirectBuffer(rem);
            ByteBuffer bb = ByteBuffer.allocate(rem);
            return bb;
        } else {
            return old;
        }
    }

    public static void checkByteBuffer(ByteBuffer bb, ByteBuffer old, String s) {
        if(bb == null) {
            System.out.println("FAVTrigger: check byte buffer, bb is null:"+old.hashCode()+", old pos:"+old.position()+", old limit:"+old.limit()+", "+s);
        } else {
            System.out.println("FAVTrigger: check byte buffer, bb not null:"+bb.hashCode()+"||"+old.hashCode()
            +", bb pos:"+old.position()+", bb limit:"+old.limit()
            +", old pos:"+old.position()+", old limit:"+old.limit()+", "+s);
        }
    }

    public static boolean isTracePath(String s){
		if(!Configuration.USE_FAULT_FUZZ) {
			return false;
		}
		String path = getRecordPath();
		File recordFile = new File(path);
		return recordFile.getAbsolutePath().equals(s);
	}
	public static boolean shouldSkip(){
		return true;
	}
	public static FileOutputStream getRecordOutStream() {
		MyLogger.debug("getRecordOutStream begin!");
		// if(!Configuration.USE_FAV || !Configuration.RECORD_PHASE) {
		if(!Configuration.USE_FAULT_FUZZ) {
			return null;
		}
        List<String> callstack = getCallStack(Thread.currentThread(), 3);
		MyLogger.debug("callstack is: " + JSONObject.toJSONString(callstack));
		// System.out.println("*****check recordoutstream******"+callstack);
        if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger")) {
			return null;
        }
		MyLogger.debug("are entering  getRecordPath()...");
		// System.out.println("*****return nonnull recordoutstream******"+callstack);
		String path = getRecordPath();
		MyLogger.debug("path is: " + path);
		File recordFile = new File(path);
		if(!recordFile.getParentFile().exists()) {
			recordFile.getParentFile().mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(recordFile, true);
			MyLogger.debug("getRecordOutStream end!");
			return out;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static FileOutputStream getRecordOutStreamInUserAPI(List<String> callstack) {
		MyLogger.debug("getRecordOutStreamInUserAPI begin!");
		// if(!Configuration.USE_FAV || !Configuration.RECORD_PHASE) {
		if(!Configuration.USE_FAULT_FUZZ) {
			return null;
		}
        // List<String> callstack = getCallStack(Thread.currentThread(), 3);
		MyLogger.debug("callstack is: " + JSONObject.toJSONString(callstack));
		// System.out.println("*****check recordoutstream******"+callstack);
		// if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger")) {
        if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl.save_result")) {
			return null;
        }
		MyLogger.debug("are entering  getRecordPath()...");
		// System.out.println("*****return nonnull recordoutstream******"+callstack);
		String recordPath = getRecordPath();
		MyLogger.debug("recordPath is: " + recordPath);
		File recordFile = new File(recordPath);
		if(!recordFile.getParentFile().exists()) {
			recordFile.getParentFile().mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(recordFile, true);
			MyLogger.debug("getRecordOutStreamInUserAPI end!");
			return out;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static FileOutputStream getRecordOutStreamInUserAPI() {
		MyLogger.debug("getRecordOutStreamInUserAPI begin!");
		// if(!Configuration.USE_FAV || !Configuration.RECORD_PHASE) {
		if(!Configuration.USE_FAULT_FUZZ) {
			return null;
		}
        List<String> callstack = getCallStack(Thread.currentThread(), 3);
		MyLogger.debug("callstack is: " + JSONObject.toJSONString(callstack));
		// System.out.println("*****check recordoutstream******"+callstack);
		// if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger")) {
        if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl.save_result")) {
			return null;
        }
		MyLogger.debug("are entering  getRecordPath()...");
		// System.out.println("*****return nonnull recordoutstream******"+callstack);
		String path = getRecordPath();
		MyLogger.debug("path is: " + path);
		File recordFile = new File(path);
		if(!recordFile.getParentFile().exists()) {
			recordFile.getParentFile().mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(recordFile, true);
			MyLogger.debug("getRecordOutStreamInUserAPI end!");
			return out;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void recordFaultPoint(FAVEntry entry) throws IOException {
		String path = entry.PATH;
		MyLogger.log("recordFaultPoint begin! path:"+path);
		// FileOutputStream out = RecordTaint.getRecordOutStream();
		FileOutputStream out = RecordTaint.getRecordOutStreamInUserAPI(entry.CALLSTACK);
		if(out == null || path == null) {
			return;
		}
		MyLogger.log("out fd is :" + out.getFD().toString());
		long timestamp = RecordTaint.getTimestamp();
		Thread thread = Thread.currentThread();
		long threadId = thread.getId();
		int threadObjId = thread.hashCode();
		List<String> callstack = entry.CALLSTACK;
		MyLogger.log("callstack is :"+JSONObject.toJSONString(callstack));
		/*
		 * @authors Wenhan Feng
		 * Generate a new entry to reflect the fault point in this execution.
		 * It is not a final design. I haven't decided how to design this part.
		 */
		FAVEntry nEntry = new FAVEntry(timestamp, threadId, threadObjId, path, callstack);
		nEntry.recPosition = FaultPos.BEFORE.toString();
		nEntry.newCovs = 0;

		MyLogger.log("start record...");
		if(Configuration.ASYC_TRACE) {
		    ArrayList<FAVEntry> entries = RecordsHandler.traces.get(getRecordPath());
	        if(entries == null) {
	            entries = new ArrayList<FAVEntry>();
	        }
	        entries.add(nEntry);
	        RecordsHandler.traces.put(getRecordPath(), entries);
	        RecordsHandler.outs.put(getRecordPath(), out);
		} else {
		    RecordsHandler.recordAnEntry(out, nEntry);
		    out.close();
		}
		MyLogger.log("recordFaultPoint end! path:"+nEntry.PATH);
	}

	public static void recordFaultPoint(String path) throws IOException {
		MyLogger.log("recordFaultPoint begin! path:"+path);
		// FileOutputStream out = RecordTaint.getRecordOutStream();
		FileOutputStream out = RecordTaint.getRecordOutStreamInUserAPI();
		if(out == null || path == null) {
			return;
		}
		MyLogger.log("out fd is :" + out.getFD().toString());
		long timestamp = RecordTaint.getTimestamp();
		Thread thread = Thread.currentThread();
		List<String> callstack = getCallStack(thread, 4);
		MyLogger.log("callstack is :"+JSONObject.toJSONString(callstack));
		FAVEntry entry = new FAVEntry(timestamp, thread.getId(), thread.hashCode(), path, Taint.emptyTaint(), callstack);
		entry.recPosition = FaultPos.BEFORE.toString();
//		int finds = JavaAfl.hasNewBits(JavaAfl.last_io_map, JavaAfl.map);
		entry.newCovs = 0;

		MyLogger.log("start record...");
		if(Configuration.ASYC_TRACE) {
		    ArrayList<FAVEntry> entries = RecordsHandler.traces.get(getRecordPath());
	        if(entries == null) {
	            entries = new ArrayList<FAVEntry>();
	        }
	        entries.add(entry);
	        RecordsHandler.traces.put(getRecordPath(), entries);
	        RecordsHandler.outs.put(getRecordPath(), out);
		} else {
		    RecordsHandler.recordAnEntry(out, entry);
		    out.close();
		}
		MyLogger.log("recordFaultPoint end! path:"+path);
	}

	public static void recordTaintEntry(long timestamp, FileOutputStream out, String path, byte b, Taint taint, String faultPos) throws IOException {
		// System.out.println("invoke recordTaintEntry");
		// MyLogger.log("invoke recordTaintEntry");
		//if(out == null || taint == null || path == null) {
	    if(out == null || path == null) {
			return;
		}
//	    if((taint == null || taint.isEmpty()) && (!path.startsWith(FAVPathType.CREALC.toString()))
//	    		&& (!path.startsWith(FAVPathType.DELLC.toString())) && (!path.startsWith(FAVPathType.OPENLC.toString()))) {
//			//TODO: consider in the future
//	        //taint = Taint.withLabel(SpecialLabel.CONSTANT);
//			return;
//	    }
		for(String str:Configuration.FILTER_PATHS) {
			if(path.startsWith(str)) {
				return;
			}
		}

		Thread thread = Thread.currentThread();
		List<String> callstack = getCallStack(thread, 4);

		FAVEntry entry = new FAVEntry(timestamp, thread.getId(), thread.hashCode(), path, Taint.emptyTaint(), callstack);
		entry.recPosition = faultPos;
//		int finds = JavaAfl.hasNewBits(JavaAfl.last_io_map, JavaAfl.map);
		entry.newCovs = 0;
//		JavaAfl.last_io_map = Arrays.copyOf(JavaAfl.map, JavaAfl.map.length);

		if(Configuration.ASYC_TRACE) {
		    ArrayList<FAVEntry> entries = RecordsHandler.traces.get(getRecordPath());
	        if(entries == null) {
	            entries = new ArrayList<FAVEntry>();
	        }
	        entries.add(entry);
	        RecordsHandler.traces.put(getRecordPath(), entries);
	        RecordsHandler.outs.put(getRecordPath(), out);
		} else {
		    RecordsHandler.recordAnEntry(out, entry);
		    out.close();
		}

	}

	public static void recordTaintsEntry(long timestamp,FileOutputStream out, String path, byte[] bytes, Taint[] taints, int off, int len, String md5) throws IOException {
		MyLogger.log("invoke recordTaintsEntry");
		System.out.println("invoke recordTaintsEntry");
		//if(out == null || taints == null || path == null) {
	    if(out == null || path == null || bytes == null) {
			return;
		}
		for(String str:Configuration.FILTER_PATHS) {
			if(path.startsWith(str)) {
				return;
			}
		}

		int length = off + len > bytes.length? bytes.length - off : len;
		int old_length = bytes.length;
		if(length > 0) {
		    Taint rst = Taint.emptyTaint();
            Thread thread = Thread.currentThread();
            List<String> callstack = getCallStack(thread);

            FAVEntry entry = new FAVEntry(timestamp, thread.getId(), thread.hashCode(), path, rst, callstack);
            if(Configuration.ASYC_TRACE) {
                ArrayList<FAVEntry> entries = RecordsHandler.traces.get(getRecordPath());
                if(entries == null) {
                    entries = new ArrayList<FAVEntry>();
                }
                entries.add(entry);
                RecordsHandler.traces.put(getRecordPath(), entries);
                RecordsHandler.outs.put(getRecordPath(), out);
            } else {
            	/*
                RecordsHandler.recordAnEntry(out, entry);
                out.close();
                */
            	ArrayList<FAVEntry> entries = RecordsHandler.traces.get(getRecordPath());
                if(entries == null) {
                    entries = new ArrayList<FAVEntry>();
                }
                entries.add(entry);
                RecordsHandler.traces.put(getRecordPath(), entries);
                RecordsHandler.outs.put(getRecordPath(), out);
            }
		}
	}

	public static void printString(String s) {
	    String callstack = getCallStack(Thread.currentThread()).toString();
	    System.out.println(s+"   "+callstack);
	}

	public static void recordString(FileOutputStream out, String s) throws IOException {
		if(out == null || s == null) {
			return;
		}
		String callstack = getCallStack(Thread.currentThread()).toString();

		out.write(s.getBytes());
		printLine(out);
		out.write(callstack.getBytes());
		printLine(out);
		printLine(out);

		out.close();
	}

	public static void printLine(FileOutputStream out) throws IOException {
		if(out == null) {
			return;
		}

		String lineSeparator = java.security.AccessController.doPrivileged(
	            new sun.security.action.GetPropertyAction("line.separator"));
		out.write(lineSeparator.getBytes());
	}

	public static int stackIndex = 0;
	// public static int stackIndex = 9;
	public static List<String> getCallStack(Thread thread){
    	StackTraceElement[] callStack;
    	callStack = thread.getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = stackIndex; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
    	return callStackString;
	}

	public static List<String> getCallStack(Thread thread, int startIdx){
    	StackTraceElement[] callStack;
    	callStack = thread.getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = startIdx; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
    	return callStackString;
	}
}