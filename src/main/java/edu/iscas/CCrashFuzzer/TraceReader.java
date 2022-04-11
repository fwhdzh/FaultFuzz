package edu.iscas.CCrashFuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;

public class TraceReader {
	private File traceDir;
	//store records for every process file
	public int total = 0;
	public static List<IOPoint> ioPoints = new ArrayList<IOPoint>();

	public ConcurrentHashMap<Integer, AtomicInteger> uniqueEntryToAppearIdx = new ConcurrentHashMap<Integer, AtomicInteger>();

	public TraceReader(String traceDir) {
		File file = new File(traceDir);
		if(!file.exists()){
        	System.out.println("The trace path doesn't exist, please check the path!");
        	return;
        }
		if(!file.isDirectory()) {
			System.out.println("The trace path is not a directory, please check the path!");
        	return;
		}
		this.traceDir = file;
	}

	public void readTraces() {
		if(traceDir==null || !traceDir.exists() || !traceDir.isDirectory()) {
			return;
		}
		Stat.deleteEmptyFile(traceDir);
		ioPoints.clear();

		File[] files = traceDir.listFiles();
		System.out.println("The size of the trace files is "+FileUtils.sizeOfDirectory(traceDir)+" bytes.");
		System.out.println("Going to handle "+files.length+" files.");

		CountDownLatch mDoneSignal = new CountDownLatch(files.length);

		for(File f:files) {
			ReadTraceThread thread = new ReadTraceThread(f, mDoneSignal, this);
			thread.start();
		}

		try {
			mDoneSignal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ioPoints.sort(Comparator.comparingLong(a -> a.TIMESTAMP));
		
		for(IOPoint sortedRec: ioPoints) {
			AtomicInteger appearIdx = uniqueEntryToAppearIdx.computeIfAbsent(sortedRec.computeIoID(), k -> new AtomicInteger(0));
			sortedRec.appearIdx = appearIdx.incrementAndGet();
        }
		
//		if(Conf.MANUAL) {
//			for(IOPoint p:ioPoints) {
//				System.out.println("timestamp: "+p.TIMESTAMP);
//				System.out.println("new covs: "+p.newCovs);
//				System.out.println(p.toString());
//	        	Scanner scan = new Scanner(System.in);
//	        	scan.nextLine();
//			}
//        }
//		for(IOPoint sortedRec: ioPoints) {
//			if(sortedRec.ioID == 1075509077) {
//				System.out.println("!!!!!!!!!!!!!!!!!!!!!read 1075509077!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			    System.out.println(sortedRec);
//			    Scanner scan = new Scanner(System.in);
//	        	scan.nextLine();
//			}
//        }
		

		System.out.println("Get "+ioPoints.size()+" records");
	}

	public static enum TraceItem {
		START, 
		TIMESTAMP,
		THREADID,
		THREADOBJ,
		PATH,
		FAULTPOS,
		NEWCOV,
		TAINT,
		CALLSTACK,
		END
	}
	
	public static class ReadTraceThread extends Thread {
		private final File procFile;
		private final CountDownLatch mDoneSignal;
		private TraceReader reader;
		ArrayList<IOPoint> records;

		public ReadTraceThread(File f, CountDownLatch mDoneSignal, TraceReader reader) {
			super();
			this.procFile = f;
			this.mDoneSignal = mDoneSignal;
			this.reader = reader;
			records = new ArrayList<IOPoint>();
			// TODO Auto-generated constructor stub
		}

		@Override
		public synchronized void start() {
			// TODO Auto-generated method stub
			super.start();
		}

		@Override
		public void run() {
			String ipProcId = procFile.getName().trim().replace("_", "/");
			String ip = ipProcId.substring(0, ipProcId.lastIndexOf("-"));
			String procId = ipProcId.substring(ipProcId.lastIndexOf("-")+1, ipProcId.length());

			ArrayList<IOPoint> createFileRecords = new ArrayList<IOPoint>();
			ArrayList<IOPoint> deleteFileRecords = new ArrayList<IOPoint>();
			ArrayList<IOPoint> openFileRecords = new ArrayList<IOPoint>();

			for(File file:procFile.listFiles()) {
				try {
					FileReader fileReader;
					fileReader = new FileReader(file);

					BufferedReader br = new BufferedReader(fileReader);
		            String lineContent = null;
		            int recCount = 0;
		            int recEntryIdx = 0;
		            IOPoint point = null;
		            while((lineContent = br.readLine()) != null){
		            	//prepare to read next record
		            	if(lineContent.trim().equals(TraceItem.START.toString())) {
		            		point = new IOPoint();
		            	} else if (lineContent.trim().startsWith(TraceItem.TIMESTAMP.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		point.ip = ip;
		            		point.procID = procId;
		            		try {
		            			point.TIMESTAMP = Long.parseLong(lineContent.trim().substring(TraceItem.TIMESTAMP.toString().length()+1));
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().startsWith(TraceItem.THREADID.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		try {
		            			point.THREADID = Long.parseLong(lineContent.trim().substring(TraceItem.THREADID.toString().length()+1));
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().startsWith(TraceItem.THREADOBJ.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		try{
		            			point.THREADOBJ = Integer.parseInt(lineContent.trim().substring(TraceItem.THREADOBJ.toString().length()+1));
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().startsWith(TraceItem.PATH.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		try{
		            			point.PATH = lineContent.trim().substring(TraceItem.PATH.toString().length()+1);
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().startsWith(TraceItem.FAULTPOS.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		String content = lineContent.trim().substring(TraceItem.FAULTPOS.toString().length()+1);
		            		if(content.trim().equals(FaultPos.BEFORE)) {
		            			point.pos = FaultPos.BEFORE;
		            		} else if (content.trim().equals(FaultPos.AFTER)) {
		            			point.pos = FaultPos.BEFORE;
		            		}
		            	} else if (lineContent.trim().startsWith(TraceItem.NEWCOV.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		try{
		            			point.newCovs = Integer.parseInt(lineContent.trim().substring(TraceItem.NEWCOV.toString().length()+1));
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().startsWith(TraceItem.TAINT.toString()+":")) {
		            		continue;
		            	} else if (lineContent.trim().startsWith(TraceItem.CALLSTACK.toString()+":")) {
		            		if(point == null) {
		            			continue;
		            		}
		            		String content = lineContent.substring(TraceItem.CALLSTACK.toString().length()+1);
		            		try{
		            			List<String> callstack = new ArrayList<String>(Arrays.asList(content.substring(1, content.length()-1).split(", ")));
		            			point.CALLSTACK = callstack;
			            		point.ioID = point.computeIoID();
		            		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		            	} else if (lineContent.trim().equals(TraceItem.END.toString())) {
		            		if(point != null && point.CALLSTACK != null && !point.CALLSTACK.isEmpty()
		            				&& point.ip != null && point.PATH != null) {
		            			if(point.pos == null) {
		            				point.pos = FaultPos.BEFORE;
		            			}
			            		recCount++;
	                            records.add(point);
		            			if(point.PATH.startsWith("CREALC")) {
		            				createFileRecords.add(point);
		            			} else if (point.PATH.startsWith("DELLC")) {
		            				deleteFileRecords.add(point);
		            			} else if (point.PATH.startsWith("OPENLC")) {
//		            				openFileRecords.add(rec);
		            			}
		            		}
	            			point = null;
		            	}
		            }
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

            records.sort(Comparator.comparingLong(a -> a.TIMESTAMP));
            synchronized(ioPoints){
            	ioPoints.addAll(records);
            }
            
			mDoneSignal.countDown();
		}
	}
}
