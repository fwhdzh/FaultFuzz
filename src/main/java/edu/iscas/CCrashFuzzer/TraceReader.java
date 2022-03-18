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
	static List<IOPoint> ioPoints = new ArrayList<IOPoint>();

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
		            	if(recEntryIdx == 8) {
		            		recEntryIdx = 0;
		            		continue;
		            	}
		            	if(recEntryIdx == 0) {
		            		point = new IOPoint();
		            		point.ip = ip;
		            		point.procID = procId;
		            		point.TIMESTAMP = Long.parseLong(lineContent.trim());
		            		recEntryIdx++;
		            	} else if (recEntryIdx == 1) {
		            		point.THREADID = Long.parseLong(lineContent.trim());
		            		recEntryIdx++;
		            	} else if (recEntryIdx == 2) {
		            		point.THREADOBJ = Integer.parseInt(lineContent.trim());
		            		recEntryIdx++;
		            	} else if (recEntryIdx == 3) {
		            		point.PATH = lineContent.trim();
		            		recEntryIdx++;
		            	} else if (recEntryIdx == 4) {
		            		if(lineContent.trim().equals(FaultPos.BEFORE)) {
		            			point.pos = FaultPos.BEFORE;
		            		} else if (lineContent.trim().equals(FaultPos.AFTER)) {
		            			point.pos = FaultPos.AFTER;
		            		}
		            		recEntryIdx++;//md5
		            	} else if (recEntryIdx == 5) {
		            		point.newCovs = Integer.parseInt(lineContent.trim());
		            		recEntryIdx++;//md5
		            	} else if (recEntryIdx == 6) {//taint
		            		String labelsContent = "";
		            		
		            		recEntryIdx++;
		            	} else if (recEntryIdx == 7) {
		            		List<String> callstack = new ArrayList<String>(Arrays.asList(lineContent.substring(1, lineContent.length()-1).split(", ")));
		            		point.CALLSTACK = callstack;
		            		point.ioID = point.computeIoID();
		            		recCount++;
                            records.add(point);
	            			if(point.PATH.startsWith("CREALC")) {
	            				createFileRecords.add(point);
	            			} else if (point.PATH.startsWith("DELLC")) {
	            				deleteFileRecords.add(point);
	            			} else if (point.PATH.startsWith("OPENLC")) {
//	            				openFileRecords.add(rec);
	            			}
		            		recEntryIdx++;
		            	}
		            }
				} catch (NumberFormatException | IOException e) {
					// TODO Auto-generated catch block
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
