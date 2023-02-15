/**
 * Copyright 2018  Jussi Judin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.iscas.tcse.favtrigger.instrumenter.cov;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordsHandler;

public class JavaAfl implements Thread.UncaughtExceptionHandler {
    // This is here so that this class won't be accidentally instrumented.
    public static final String INSTRUMENTATION_MARKER = "__JAVA-AFL-INSTRUMENTED-CLASSFILE__";
    public static File coverOutFile;

    public void uncaughtException(Thread t, Throwable e) {
//        javafl.JavaAfl._handle_uncaught_exception();
        e.printStackTrace(System.err);
        System.exit(1);
    }

    // Map size link between C code Java:
//    static private native int _get_map_size();

    /* Map size for the traced binary (2^MAP_SIZE_POW2). Must be greater than
    2; you probably want to keep it under 18 or so for performance reasons
    (adjusting AFL_INST_RATIO when compiling is probably a better way to solve
    problems with complex programs). You need to recompile the target binary
    after changing this - otherwise, SEGVs may ensue. */

    public static int _get_map_size() {
    	int MAP_SIZE_POW2 = 16;
    	int MAP_SIZE = (1 << MAP_SIZE_POW2);
    	return MAP_SIZE;
    }

    // These are fields that the instrumentation part uses to do its thing:
    public static byte[] map;
    public static byte[] last_io_map;
    public static int prev_location;
    public static CopyOnWriteArrayList<String> debug_log;
    public static int afl_port;
    public static boolean main_started;
    public static Thread save_records_td;
    public static Thread flush_ios_td;
    private static final Object saving = new Object();
    private static ServerSocket saveRecsServer;

    // If you change the string value of this, you also need to change
    // the corresponding value at JavaAflInject.java file!
    // This enables only passing 64 kilobytes of data. It is more than
    // enough with the help of gzip compression on Linux even when
    // there is tons of debugging data added to the resulting JNI
    // library.
    private static final String _jni_code = "<INJECT-JNI>";

    static {
    	/*
        java.io.File jni_target = null;
        try {
            byte jni_code_compressed[] = _jni_code.getBytes("ISO-8859-1");
            java.util.zip.GZIPInputStream input = new java.util.zip.GZIPInputStream(
                new java.io.ByteArrayInputStream(jni_code_compressed));
            jni_target = java.io.File.createTempFile("libjava-afl-", ".so");
            java.io.FileOutputStream output = new java.io.FileOutputStream(jni_target);
            byte buffer[] = new byte[4096];
            int read = input.read(buffer, 0, buffer.length);
            while (read > 0) {
                output.write(buffer, 0, read);
                read = input.read(buffer, 0, buffer.length);
            }
            System.load(jni_target.getAbsolutePath());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (jni_target != null) {
                // We need to explicitly delete a file here instead of
                // using File.deleteOnExit(), as the JNI
                // instrumentation can exit from JVM without running
                // exit handlers.
                jni_target.delete();
            }
        }
        */
        map = new byte[_get_map_size()];
        last_io_map = new byte[_get_map_size()];
        debug_log = new CopyOnWriteArrayList<String>();
        main_started = false;
//        coverOutFile = new File(coverOutFile.getAbsolutePath()+"/"+FAVTaint.getIP().replace("/", "_")+"-"+FAVTaint.getProcessID()+"/"+"fuzzcov");
    }

    public static void _before_main() {
    	System.out.println("this is before main!");
    	main_started = true;
        edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl._init(false);
    }
    public static void mark(int pre_loc, int cur_loc) {
    	debug_log.add("From "+pre_loc+" to "+cur_loc
    			+", cur_loc change "+(cur_loc >> 1)+". total locs:");
    }
    public static void mark(int pre_loc, int cur_loc, String info) {
    	System.out.println("From "+pre_loc+" to "+cur_loc
    			+", cur_loc change "+(cur_loc >> 1)+". total locs:"+info);
    }
    public static void _after_main() {
    	System.out.println("this is after main!");
    	main_started = false;
    	save_result();
    	if(saveRecsServer != null) {
    		try {
				saveRecsServer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
    	}
    	save_records_td.interrupt();
    	flush_ios_td.interrupt();
    }

    public static void noting() {
    	debug_log.add("this is noting!");
    }
    public static void save_result() {
    	synchronized(saving) {
    		try {
//    			FileUtils.writeByteArrayToFile(new File(""), trace_map.data);
//        		System.out.println("_after_main goiong to write:"+(coverOutFile != null?true:false));
//        		System.out.println("this is save_result!");
    			if(coverOutFile != null) {
    				debug_log.add("this is after main print cov file!");

    				FileOutputStream out = new FileOutputStream(coverOutFile);
//    				System.out.println("CrashFuzz: mark covered blocks"+coveredBlocks(data)+" | "+coveredBlocks2(data));
    				out.write(map, 0, map.length);
    				out.flush();
    				out.close();

    				String parent = coverOutFile.getParent();
    				String logfile = "fuzzcov-recs";
    				if(parent == null) {
    					logfile = parent+"/fuzzcov-recs";
    				}
    				FileOutputStream deout = new FileOutputStream(logfile);
    				String lineSeparator = java.security.AccessController.doPrivileged(
    			            new sun.security.action.GetPropertyAction("line.separator"));
    				List<String> curlogs = new ArrayList<String>();
    				curlogs.addAll(debug_log);
    				for(String s:debug_log) {
    					deout.write(s.getBytes());
    					deout.write(lineSeparator.getBytes());
    				}
    				deout.flush();
    				deout.close();
    			}

    			System.out.println("start output traces to file:"+RecordsHandler.traces.keySet().size());
                for(String file:RecordsHandler.traces.keySet()) {
                    FileOutputStream out = RecordsHandler.outs.get(file);
                    if(out == null) {
                        continue;
                    }
                    try {
                    	List<FAVEntry> entries = new ArrayList<FAVEntry>();
                        int range = RecordsHandler.traces.get(file).size();
                        List<FAVEntry> tmp = RecordsHandler.traces.get(file).subList(0, range);
                        entries.addAll(tmp);
                        tmp.clear();
                        for(FAVEntry entry:entries) {
                        	RecordsHandler.recordAnEntry(out, entry);
                        }
                        out.close();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
    			System.out.println("save_result end");
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }

    public static int hasNewBits(byte[] old_bytes, byte[] new_bytes) {
    	int finds = 0;
		for(int i = 0; i< new_bytes.length; i++) {
			if(new_bytes[i]>0 && old_bytes[i] ==0) {
				finds++;
			}
		}
    	return finds;
    }

    public static boolean waitStable() {
    	int sameCount = 5;
    	byte[] last_map = new byte[map.length];
    	int maxWaitM = 5;
    	int waitTime = 0;
    	/*
    	while(sameCount > 0) {
    		if(hasNewBits(last_map, map)>0) {
    			sameCount = 5;
    		} else {
    			sameCount--;
    			if(sameCount == 0) {
    				return true;
    			}
    		}
    		last_map = Arrays.copyOf(map, map.length);
    		try {
				Thread.currentThread().sleep(1000);
				waitTime++;
				if(waitTime > maxWaitM*60) {
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	*/
    	return true;
    }

    protected static void _init(boolean is_persistent) {
    	if(coverOutFile != null) {
			debug_log.add("this is after main print cov file!");
//			System.out.println("_after_main goiong to write:"+map[5451^58526]);
			if(coverOutFile.exists()) {//load last trace map and combine
				byte[] virgin_bits = new byte[_get_map_size()];//covered
				FileInputStream tracedFileIn;
				try {
					tracedFileIn = new FileInputStream(coverOutFile);
					tracedFileIn.read(virgin_bits);
					tracedFileIn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for(int i = 0; i< virgin_bits.length; i++) {
					map[i] = virgin_bits[i];
				}
			}
    	}
//        _init_impl(is_persistent);
        JavaAfl handler = new JavaAfl();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            	System.out.println("this is after main shutdown hook!");
                // TODO bad exit combination when in persistent mode...
                JavaAfl._after_main();
            }
        });

        save_records_td = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				if(JavaAfl.afl_port != Integer.MIN_VALUE) {
					try{
			            saveRecsServer = new ServerSocket(JavaAfl.afl_port);
			            int counter = 0;
			            System.out.println("ALF Server Started ....");
			            while(main_started){
			              counter++;
			              Socket serverClient = saveRecsServer.accept();  //server accept the client connection request
			              AflCliHandler client = new AflCliHandler(serverClient,counter); //send  the request to a separate thread
			              client.start();
			              Thread.currentThread().sleep(500);
			            }
			            System.out.println("ALF Server exits ....");
			         } catch(Exception e) {
			        	  e.printStackTrace();
			         }
				}
			}
        };
        save_records_td.start();

        flush_ios_td = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				System.out.println("ALF save results periodically ....");
				while(main_started){
		            try {
						Thread.currentThread().sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
					}
		            save_result();
		        }
			}
        };
        flush_ios_td.start();
    }

//    static protected native void _init_impl(boolean is_persistent);
//    static public native void _handle_uncaught_exception();
//    static public native void _after_main();
//
//    static protected native void _send_map();
}
