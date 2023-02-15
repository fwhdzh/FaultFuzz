package edu.iscas.tcse.favtrigger.instrumenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;

public class CoverageMap {
	public static int MAP_SIZE = Integer.MAX_VALUE;
//	public static BitArray trace_map;
    public static byte[] data = null;
	public static File coverOutFile;
	public static boolean running;
	public static List<String> includes;

	public static boolean applyCov(String cname) {
		if(cname.contains("$$Lambda$")) {
			return false;
		}
		if(includes == null) {
			return !(cname.startsWith("java/") || cname.startsWith("javax/")
					|| cname.startsWith("org/ietf/jgss")
					|| cname.startsWith("org/omg")
					|| cname.startsWith("org/w3c")
					|| cname.startsWith("org/xml"));
		}
		for(String s:includes) {
			if(cname.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMNameDescMatch(String cname, String mname, String desc, String[] secs) {
//		if(secs.length == 1) {//do not specify mname
//			return true;
//		} else if(secs.length >= 2 && (mname.equals(secs[1].trim()) || mname.equals(secs[1].trim()+TaintUtils.METHOD_SUFFIX)
//				|| mname.equals(secs[1].trim()+TaintUtils.METHOD_SUFFIX_UNINST))) {
//			return true; //TODO: add logic for desc comparision in the future
//		} else {
//			return false;
//		}
		if(secs.length == 1) {//do not specify mname
			return true;
		} else {
			return (secs.length >= 2 && (mname.equals(secs[1].trim()) || mname.equals(secs[1].trim()+TaintUtils.METHOD_SUFFIX)
					|| mname.equals(secs[1].trim()+TaintUtils.METHOD_SUFFIX_UNINST)));
		}
	}

	public static boolean isInList(String cname, String mname, String desc, List<String> list) {
		for(String element:list) {
			String[] secs = element.split(" ");
			if(secs.length >= 1) {
				if(secs[0].trim().endsWith("*")) {
					String realCname = secs[0].trim().substring(0, secs[0].length()-1);
					if(cname.startsWith(realCname)) {
						if(isMNameDescMatch(cname, mname, desc, secs)) {
							return true;
						} else {
							continue;
						}
					} else {
						continue;
					}
				} else if (secs[0].trim().endsWith("$")) {
					String realCname = secs[0].trim().substring(0, secs[0].length()-1);
					if(cname.equals(realCname) || cname.startsWith(realCname+"$")) {
						if(isMNameDescMatch(cname, mname, desc, secs)) {
							return true;
						} else {
							continue;
						}
					} else {
						continue;
					}
				} else if (cname.equals(secs[0].trim())) {
					if(isMNameDescMatch(cname, mname, desc, secs)) {
						return true;
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
		}
		return false;
	}

	public static boolean useAFLInst(String cname, String mname, String desc) {
//		return cname.startsWith("org/apache/zookeeper/server/");
		if(Configuration.AFL_DENYLIST !=null && !Configuration.AFL_DENYLIST.isEmpty()
				&& isInList(cname, mname, desc, Configuration.AFL_DENYLIST)) {
//			if(cname.endsWith("ZooKeeperServer")) {
//				System.out.println("this is wrong"+cname+" "+mname+" "+desc);
//				for(String s: Configuration.AFL_DENYLIST) {
//					System.out.println("this is wrong deny:"+s);
//				}
//			}
			return false;
		}

		if(Configuration.AFL_ALLOWLIST == null || Configuration.AFL_ALLOWLIST.isEmpty()) {
			return true;
		} else {
			return isInList(cname, mname, desc, Configuration.AFL_ALLOWLIST);
		}
	}

	public static void startUp() {
		if(Configuration.USE_FAV && data != null && coverOutFile != null) {
			System.out.println("CrashFuzz STARTUP WRITE");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                	running = false;
                	save_bit_map();
                }
            });

            Thread writeExecutionRst = new Thread() {

    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				while(running) {
    					save_bit_map();
    					try {
    						Thread.currentThread().sleep(1000);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}
    			}
    		};
    		writeExecutionRst.start();
        }
	}

	public static void initMap() {
//		if(Configuration.USE_FAV) {
//			trace_map = new BitArray(MAP_SIZE);
			running = true;
			data = new byte[MAP_SIZE];
	        Arrays.fill(data, (byte)0);
//		}
	}
	public static void setBit(int pos, boolean b) {
        if (b) {
        	data[pos] = (byte)1;
        } else {
        	data[pos] = (byte)0;
        }
//        System.out.println("setbit: covered blocks:"+coveredBlocks2(data));
    }

    public static boolean getBit(int pos) {
    	return (data[pos]) != 0;
    }

	public static enum BlockType {
		ENTER, LABEL, JUMP, BLOCK
	}
	public static int getBlockPos(String blockID) {
		int positive = Math.abs(blockID.hashCode());
		int pos = positive % MAP_SIZE;
		return pos;
	}
	public static synchronized void save_bit_map() {
		try {
//			FileUtils.writeByteArrayToFile(new File(""), trace_map.data);
			if(coverOutFile != null) {
				FileOutputStream out = new FileOutputStream(coverOutFile);
//				System.out.println("CrashFuzz: mark covered blocks"+coveredBlocks(data)+" | "+coveredBlocks2(data));
				out.write(data, 0, data.length);
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int coveredBlocks(byte[] data) {
		int sum = 0;
		for(int i = 0;i<data.length;i++) {
			int c = (data[i]);
			  while(c != 0) {
				  c &= (c-1);
				  sum++;
			  }
		}
		return sum;
	}

	public static int coveredBlocks2(byte[] data) {
		int sum = 0;
		for(int i = 0;i<data.length;i++) {
			sum += data[i];
		}
		return sum;
	}
}
