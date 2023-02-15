package edu.iscas.tcse.favtrigger.tracing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyReferenceArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedIntWithObjTag;
import edu.iscas.tcse.favtrigger.MyLogger;
//import edu.iscas.tcse.favtrigger.instrumenter.SysTime;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;

public class RecordTaint {
	//for test recording ASTORE, delete in the future
	public static void recordTaint(Taint t) throws IOException {
		System.out.println("ASTORE RECORD TAINT");
		System.out.println("ASTORE RECORD TAINT: "+t.toString());
	}

	public static int[] getByteBufferPositions(LazyReferenceArrayObjTags obj) {
		int length = obj.val.length;
		int[] rst = new int[length];
		for(int i = 0; i< length; i++) {
			ByteBuffer buffer = (ByteBuffer) obj.val[i];
			rst[i] = buffer.position();
		}
		return rst;
	}

	public static Taint[] taintsMerger(Taint[] taints1, int off1, int len1, Taint[] taints2, int off2, int len2) {
		Taint[] taint_3 = new Taint[len1+len2];
		System.arraycopy(taints1, off1, taint_3, 0, len1);
		System.arraycopy(taints2, off2, taint_3, len1, len2);
		return taint_3;
	}

	public static byte[] bytesMerger(byte[] byte_1, int off1, int len1, byte[] byte_2, int off2, int len2) {
		byte[] byte_3 = new byte[len1+len2];
		System.arraycopy(byte_1, off1, byte_3, 0, len1);
		System.arraycopy(byte_2, off2, byte_3, len1, len2);
		return byte_3;
	}

	public static LazyByteArrayObjTags getSuperBytes(List<LazyByteArrayObjTags> bytesList, List<Integer> offs, List<Integer> lens) {
		if(bytesList.size() == 0) {
			return null;
		} else if(bytesList.size() == 1) {
			byte[] bytes = new byte[lens.get(0)];
			Taint[] taints = new Taint[lens.get(0)];
			System.arraycopy(bytesList.get(0).val, offs.get(0), bytes, 0, lens.get(0));
			System.arraycopy(bytesList.get(0).taints, offs.get(0), taints, 0, lens.get(0));
			LazyByteArrayObjTags rst = new LazyByteArrayObjTags(bytes, taints);
			return rst;
		} else {
			List<LazyByteArrayObjTags> newList = new ArrayList<LazyByteArrayObjTags>();
			List<Integer> newOffs = new ArrayList<Integer>();
			List<Integer> newLens = new ArrayList<Integer>();
			for(int i = 0; i < bytesList.size();) {
				if((i+1)<bytesList.size()) {
					byte[] bytes = bytesMerger(bytesList.get(i).val, offs.get(i), lens.get(i), bytesList.get(i+1).val, offs.get(i+1), lens.get(i+1));
					Taint[] taints = taintsMerger(bytesList.get(i).taints, offs.get(i), lens.get(i), bytesList.get(i+1).taints, offs.get(i+1), lens.get(i+1));
					LazyByteArrayObjTags obj = new LazyByteArrayObjTags(bytes, taints);
					newList.add(obj);
					newOffs.add(0);
					newLens.add(bytes.length);
				} else {
					newList.add(bytesList.get(i));
					newOffs.add(offs.get(i));
					newLens.add(lens.get(i));
				}
				i += 2;
			}
			return getSuperBytes(newList, newOffs, newLens);
		}
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
		if(Configuration.USE_FAV) {
			// System.load(Configuration.FAV_HOME+"/systime/systime.so");
	        // SysTime systime = new SysTime();
            // long timestamp = systime.rdtscp();
			long timestamp = System.currentTimeMillis();
            return timestamp;
		} else {
			return Long.MIN_VALUE;
		}
	}

	public static int getMsgID() {
		if(Configuration.USE_MSGID || Configuration.YARN_RPC || Configuration.FOR_YARN
		        || Configuration.FOR_MR || Configuration.MR_RPC || Configuration.FOR_HDFS
				|| Configuration.FOR_HBASE || Configuration.HDFS_RPC || Configuration.HBASE_RPC
				|| Configuration.FOR_ZK || Configuration.ZK_CLI) {
            Random rand = new Random();
	        int temp = rand.nextInt(Integer.MAX_VALUE);//exclusive Max_value
	        return temp;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	private static final char[] HEXES = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

	public static String getMD5HashForBytes(byte[] block, int off, int len) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
	        digest.update(block, off, len);
	        return bytes2Hex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			byte[] rst = new byte[16];
			int i = 0;
			for(; i < 16 && i< len; i++) {
				rst[i] = block[off+i];
			}
			for(; i < 16; i++) {
				rst[i] = (byte) 0;
			}
			return bytes2Hex(rst);
		}
	}

	public static String getMD5Hash(byte b) {
		byte[] bytes = new byte[1];
		bytes[0] = b;
		return bytes2Hex(bytes);
	}

	public static String getRecordPath() {
		// MyLogger.log("getRecordPath is invoked");
	    String path = Configuration.FAV_RECORD_PATH+FAVTaint.getIP().replace("/", "_")+"-"+FAVTaint.getProcessID()+"/"+Thread.currentThread().getId();
        //String path = Configuration.FAV_RECORD_PATH+"tmp";
	    return path;
	}

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

    public static TaintedIntWithObjTag restoreReadByteBufferResult(TaintedIntWithObjTag read, ByteBuffer old, LazyByteArrayObjTags oldData, ByteBuffer buf,
            String cname, String mname, String desc, String type, String tag, String linkSource) {
        TaintedIntWithObjTag rst = new TaintedIntWithObjTag(read.taint, read.val);
        if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
            if(read.val > 0) {
                int newPos = buf.position();
                int newLim = buf.limit();
                buf.flip(); //ready for read
                int currentRead = buf.limit()/5;
                int lastRead = (buf.limit() - read.val)/5;
                rst.val = currentRead - lastRead;
                for(int i = lastRead; i<currentRead; i++) {
                    buf.position(i*5);
                    int pos = old.position();
                    old.put(buf.get());
                    byte[] msgIdBytes = new byte[4];
                    buf.get(msgIdBytes, 0, 4);
                    int msgID = byteArrayToInt(msgIdBytes);
                    //System.out.println("!!!!!!!!!!!FAVTrigger: received wrapped "+(read.val)+":"+rst.val+" bytebuffer, a byte with msgid:"+msgID+", from "+linkSource+", "+getCallStack(Thread.currentThread()));
                    if(Configuration.USE_FAV && Configuration.RECORD_PHASE) {
                        if(oldData != null) {
                            Taint nt = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, FAVPathType.FAVMSG.toString()+":"+linkSource+"&"+msgID);
                            oldData.setTaint(pos, nt);
                        } else {//cannot handle DirectByteBuffer
                            //System.out.println("!!!!!!!!!!!FAVTrigger: with msgid, original bytebuffer for read has null LazyByteArrayObjTags:"+getCallStack(Thread.currentThread()));
                        }
                    }
                }
                buf.position(newPos);
                buf.limit(newLim);
            }
        // } else if (Configuration.JDK_MSG && Configuration.USE_FAV && Configuration.RECORD_PHASE) {
        } else if (Configuration.JDK_MSG && Configuration.USE_FAV) {
            for(int i = 0; i<rst.val; i++) {
                if(oldData != null) {
                    Taint nt = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, FAVPathType.FAVMSG.toString()+":"+linkSource);
                    oldData.setTaint(old.position()-1-i, nt);
                } else {//cannot handle DirectByteBuffer
                    //System.out.println("!!!!!!!!!!!FAVTrigger: original bytebuffer for read has null LazyByteArrayObjTags:"+getCallStack(Thread.currentThread()));
                }
            }
        }
        return rst;
    }

	public static byte[] newBytesWithMsgID(int msgID, byte[] old, int off, int len) {
	    byte[] rst;
	    int actual_len = off+len > old.length? old.length-off:len;
	    int rst_len = 5*actual_len;
	    if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
	        byte[] msgIDBytes = intToByteArray(msgID);
	        rst = new byte[rst_len];
	        for(int i = off; i<len+off; i++) {
	            int desPos = (5*(i - off));
	            // rst[desPos] = old[i];
				System.arraycopy(old, i, rst, desPos, 1);
	            //copy one byte by one byte would cause error
	            System.arraycopy(msgIDBytes, 0, rst, desPos+1, 4);
	        }
	    } else {
	        rst = new byte[actual_len];
	        System.arraycopy(old, off, rst, 0, actual_len);
	    }
	    //System.out.println("FAVTrigger: new bytes with msgid "+msgID+", original lenght "+old.length+", new lenght "+rst.length);
        return rst;
    }

	public static byte[] newBytesWaitMsgID(byte[] old, int off, int len) {
	    byte[] rst;
	    int actual_len = off+len > old.length? old.length-off:len;
	    if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
	        rst = new byte[actual_len*5];
			// rst = new byte[actual_len];
	    } else {
	        rst = new byte[actual_len];
	    }
	    //System.out.println("FAVTrigger: new bytes wait fro msgid, original lenght "+old.length+", new lenght "+rst.length);
        return rst;
    }

	public static int restoreMsgBytes(byte[] newBytes, LazyByteArrayObjTags oldData, int off, int len, int readRst,
	        String cname, String mname, String desc, String type, String tag, String linkSource) {
	    //TODO: fix in the future, the read data could be incomplement
	    int rst = 0;
	    if(Configuration.USE_MSGID && Configuration.JDK_MSG) {
//	        for(int i = 0; i<readRst;) {
//	            //oldData.val[off + rst] = newBytes[i];
//	            System.arraycopy(newBytes, i, oldData.val, off + rst, 1);
//	            byte[] msgIDBytes = new byte[4];
//	            if(i+5 <= readRst) {
//	                System.arraycopy(newBytes, i+1, msgIDBytes, 0, 4);
//	                int msgID = byteArrayToInt(msgIDBytes);
//	                //System.out.println("FAVTrigger: restore with msgid, read a new byte with msgid "+msgID);
//	                //System.out.println("!!!!!!!!!!!FAVTrigger: received wrapped"+readRst+" bytes, a byte with msgid:"+msgID+", from "+linkSource+", "+getCallStack(Thread.currentThread()));
//	                if(Configuration.USE_FAV && Configuration.RECORD_PHASE && Configuration.JDK_MSG) {
//	                    Taint nt = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, FAVPathType.FAVMSG.toString()+":"+linkSource+"&"+msgID);
//	                    oldData.setTaint(off + rst, nt);
//	                }
//	            } else {
//	                System.err.println("!!!FAVTrigger: unexpected socket input data! "+readRst);
//	            }
//
//	            rst++;
//	            i += 5;
//	        }
	    	if(readRst % 5 == 0) {
	    		rst = readRst/5;
	    	} else {
	    		rst = readRst/5 + 1;
	    	}
	    	for(int i = 0; i<rst;i++) {
	    		System.arraycopy(newBytes, i*5, oldData.val, off + i, 1);
	    		byte[] msgIDBytes = new byte[4];
	    		System.arraycopy(newBytes, i*5+1, msgIDBytes, 0, 4);
	    		int msgID = byteArrayToInt(msgIDBytes);
	    		if(Configuration.USE_FAV && Configuration.JDK_MSG) {
                    Taint nt = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, FAVPathType.FAVMSG.toString()+":"+linkSource+"&"+msgID);
                    oldData.setTaint(off + i, nt);
                }
	    	}
	    } else {
	        System.arraycopy(newBytes, 0, oldData.val, off, readRst);
	        if(Configuration.RECORD_PHASE && Configuration.USE_FAV && Configuration.JDK_MSG) {
	            for(int i = off; i<off+readRst; i++) {
	                Taint nt = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, FAVPathType.FAVMSG.toString()+":"+linkSource);
	                oldData.setTaint(i, nt);
	            }
	        }
	        rst = readRst;
	    }
	    //System.out.println("FAVTrigger: restore with msgid, read "+readRst+", new rst "+rst);
        return rst;
    }

    public static boolean isTracePath(String s){
		if(!Configuration.USE_FAV) {
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
		// if(!Configuration.USE_FAV || !Configuration.RECORD_PHASE) {
		if(!Configuration.USE_FAV) {
			return null;
		}
        List<String> callstack = getCallStack(Thread.currentThread(), 3);
		// System.out.println("*****check recordoutstream******"+callstack);
        if(callstack.toString().contains("org.jacoco.agent") || callstack.toString().contains("edu.iscas.tcse.favtrigger")) {
			return null;
        }
		// System.out.println("*****return nonnull recordoutstream******"+callstack);
		String path = getRecordPath();
		File recordFile = new File(path);
		if(!recordFile.getParentFile().exists()) {
			recordFile.getParentFile().mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(recordFile, true);
			return out;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static void recordTaintEntry(long timestamp, FileOutputStream out, String path, byte b, Taint taint, String md5) throws IOException {
		System.out.println("invoke recordTaintEntry");
		MyLogger.log("invoke recordTaintEntry");
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
		entry.recPosition = md5;
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
//		    if(taints == null) {
//		        //rst = Taint.withLabel(SpecialLabel.CONSTANT);
//				return;
//		    } else {
//		        Taint[] actualTaints = new Taint[length];
//	            for(int i = off; i < off + len && i < old_length; i++) {
//	                actualTaints[i-off] = taints[i];
//	            }
//	            rst = Taint.combineTaintArray(actualTaints);
//	            if(rst == null || rst.isEmpty()) {
//	                //rst = Taint.withLabel(SpecialLabel.CONSTANT);
//					return;
//	            }
//		    }

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

	public static void recordTaint(FileOutputStream out, Taint t) throws IOException {
		if(out == null || t == null || t.isEmpty()) {
			return;
		}
		/*
		if(out.getFD()==FileDescriptor.out || out.getFD()==FileDescriptor.err) {
			return;
		}
		*/
		out.write(t.toString().getBytes());
		printLine(out);
	}

	public static void recordTaints(FileOutputStream out, Taint[] taints, int off, int len) throws IOException {
		if(out == null || taints == null) {
			return;
		}

		String callstack = getCallStack(Thread.currentThread()).toString();
		int old_length = taints.length;
		if(len > 0) {
			Taint[] actualTaints = new Taint[len];
			for(int i = off; i < off+len && i < old_length; i++) {
				actualTaints[i-off] = taints[i];
			}

			Taint rst = Taint.combineTaintArray(actualTaints);
			if(rst != null && !rst.isEmpty()) {
				out.write(rst.toString().getBytes());
				printLine(out);
				out.write(callstack.getBytes());
				printLine(out);
				printLine(out);
			}
		}
		out.close();
		/*
		//some taints may be same in one write, we only need to record unique taints
		Set<Taint> set = new HashSet<Taint>();
		for(int i = off; i < len; i++) {
			if(taints[i] != null && !taints[i].isEmpty()) {
        		set.add(taints[i]);
    		}
		}

		for(Taint t:set) {
			out.write(t.toString().getBytes());
			printLine(out);
		}
		*/
	}

	public static Vector<String> transJavaForMappedTasks(Vector<String> old){
	    old.remove(0);
	    old.insertElementAt("/home/gaoyu/java/jdk1.8.0_271/bin/java", 0);
	    return old;
	}

	public static void printString(String s) {
	    String callstack = getCallStack(Thread.currentThread()).toString();
	    System.out.println(s+"   "+callstack);
	}
	public static void recordString(String s) {
//	     String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//	     System.out.println("!!!!!!"+s);
	    try {
	        FileOutputStream out = new FileOutputStream("/home/gaoyu/check", true);
	        String callstack = getCallStack(Thread.currentThread()).toString();
	        out.write(s.getBytes());
	        printLine(out);
	        out.write(callstack.getBytes());
	        printLine(out);
	        printLine(out);

	        out.close();
	    } catch (IOException e) {

	    }
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