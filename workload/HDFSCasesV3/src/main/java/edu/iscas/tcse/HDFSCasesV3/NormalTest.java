package edu.iscas.tcse.HDFSCasesV3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

public class NormalTest {
	static String stopNodeSH;
	static String startNodeSH;
	static String failSH;
	static boolean check = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args[0].equals("check")) {
			check = true;
		}
		startNodeSH = args[1];
		stopNodeSH = args[2];
		failSH = args[3];
		String testName = "NormalTest";
		
		Logger.log(testName, " start .................");
		Logger.log(testName, " going get distributed filesystem ..............");
		FileSystem fs = HDFSUtil.getDFSFileSystem();
		if(fs == null) {
			Logger.log(testName, " cannot get the distributed file system.");
			return;
		}
		
		String oriContent = "When her shift was over";
//		long firstCurTime = 0;
//		try {
//			FSDataOutputStream fsDataOutputStream = fs.create(new Path("/usr/root/fav2/file01"), true);
//			Logger.log(testName, "create file /usr/root/fav2/file01:");
//			
////			if(check) {
////				FileStatus status = fs.getFileStatus(new Path("/usr/root/fav2/file01"));
////				Logger.log(testName, "length file /usr/root/fav2/file01:"+status.getLen());
////				if(status.getLen() != 0) {
////					String info = Logger.log(testName, "the file /usr/root/fav2/file01 length is not 0 after created it at overwrite mode:"+status.getLen());
////					reportFailure(info);
////				}
////			}
//			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
//			firstCurTime = System.currentTimeMillis();
//			bufferedWriter.write(String.valueOf(firstCurTime));
////	        bufferedWriter.newLine();
//	        bufferedWriter.close();
//	        Logger.log(testName, "write current time millis "+firstCurTime+" to file /usr/root/fav2/file01:");
////	        if(check) {
////				FileStatus status = fs.getFileStatus(new Path("/usr/root/fav2/file01"));
////				if(status.getLen() != String.valueOf(curTime).getBytes().length) {
////					String info = Logger.log(testName, "the file /usr/root/fav2/file01 length is not "+String.valueOf(curTime).getBytes().length+" after writing current time millis:"+status.getLen());
////					reportFailure(info);
////				}
////			}
//		} catch (IllegalArgumentException | IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			if(check) {
//				String info = Logger.log(testName, "create file /usr/root/fav2/file01 and write to file failed!");
//				reportFailure(info);
//			}
//		}
		
		
		long curTime = 0;
		try {
			FSDataOutputStream fsDataOutputStream = fs.append(new Path("/usr/root/fav2/file01"));
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
			curTime = System.currentTimeMillis();
			bufferedWriter.write(String.valueOf(curTime));
//	        bufferedWriter.newLine();
	        bufferedWriter.close();
	        Logger.log(testName, "append current time millis "+curTime+" to file /usr/root/fav2/file01:");
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "append to file /usr/root/fav2/file01 failed!");
				reportFailure(info);
			}
		}
		

		boolean isSucc = false;
		try {
			isSucc = fs.rename(new Path("/usr/root/fav2/file01"), new Path("/usr/root/final"));
			Logger.log(testName, "rename file /usr/root/fav2/file01 to /usr/root/final:"+isSucc);
			if(check) {
//				RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path("/usr/root/fav2/"), true);
//				if(files.hasNext()) {
//					String info = Logger.log(testName, "dir /usr/root/fav2/ is not null!");
//					reportFailure(info);
//				}
//				files = fs.listFiles(new Path("/usr/root/fav1/"), true);
//				List<LocatedFileStatus> actualList = new ArrayList<LocatedFileStatus>();
//				while (files.hasNext()) {
//				    actualList.add(files.next());
//				}
//				if(actualList.size() != 1) {
//					String info = Logger.log(testName, "num of files in dir /usr/root/fav1/ is not 1!");
//					reportFailure(info);
//				}
//				files = fs.listFiles(new Path("/usr/root/"), true);
//				actualList = new ArrayList<LocatedFileStatus>();
//				while (files.hasNext()) {
//				    actualList.add(files.next());
//				}
//				if(actualList.size() != 2) {
//					String info = Logger.log(testName, "num of files in dir /usr/root is not 2!");
//					reportFailure(info);
//				}
				
//				RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path("/usr/root/"), true);
//				files = fs.listFiles(new Path("/usr/root/fav1/"), true);
//				List<LocatedFileStatus> actualList = new ArrayList<LocatedFileStatus>();
//				while (files.hasNext()) {
//				    actualList.add(files.next());
//				}
//				if(actualList.size() != 2) {
//					String info = Logger.log(testName, "num of files in dir /usr/root is not 2!");
//					reportFailure(info);
//				}
			}
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "rename file /usr/root/fav2/file01 to /usr/root/final failed!");
				reportFailure(info);
			}
		}
		
//		try {
//			isSucc = fs.delete(new Path("/usr/root/fav1/"), true);
//			Logger.log(testName, "delete dir /usr/root/fav1/:"+isSucc);
//			isSucc = fs.exists(new Path("/usr/root/fav1/"));
//			if(isSucc) {
//				String info = Logger.log(testName, "dir /usr/root/fav1/ still exists!");
//				reportFailure(info);
//			}
//		} catch (IllegalArgumentException | IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			FSDataOutputStream fsDataOutputStream = fs.create(new Path("/usr/root/fav1/file01"), false);
//			Logger.log(testName, "create file /usr/root/fav1/file01:");
//		} catch (IllegalArgumentException | IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			if(check) {
//				String info = Logger.log(testName, "create file /usr/root/fav1/file01 and write to file failed!");
//				reportFailure(info);
//			}
//		}
		try {
			RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path("/usr/root/"), true);
			List<LocatedFileStatus> actualList = new ArrayList<LocatedFileStatus>();
			while (files.hasNext()) {
			    actualList.add(files.next());
			}
			if(actualList.size() != 1) {
				String info = Logger.log(testName, "num of files in dir (recursive) /usr/root/ is not 1:"+actualList.size());
				reportFailure(info);
			}
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "list files under /usr/root/ failed!");
				reportFailure(info);
			}
		}
		
		
//		String newContent = String.valueOf(firstCurTime)+String.valueOf(curTime);
//		int newLength = String.valueOf(firstCurTime).getBytes().length+String.valueOf(curTime).getBytes().length/2;
//		newContent = newContent.substring(0, newLength);
//		try {
//			isSucc = fs.truncate(new Path("/usr/root/final"), newLength);
//			Logger.log(testName, "truncate file /usr/root/final to "+newLength+":"+isSucc);
//			if(!isSucc) {
//				isSucc = fs.truncate(new Path("/usr/root/final"), newLength);
//				Logger.log(testName, "try again to truncate file /usr/root/final to "+newLength+":"+isSucc);
//			}
//		} catch (IllegalArgumentException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			if(check) {
//				String info = Logger.log(testName, "truncate file /usr/root/final failed!");
//				reportFailure(info);
//			}
//		}
		String newContent = oriContent;
		if(curTime != 0) {
			newContent = oriContent + String.valueOf(curTime);
		}
		try {
			FSDataInputStream inputStream = fs.open(new Path("/usr/root/final"));
			//Classical input stream usage
	        String out= IOUtils.toString(inputStream, "UTF-8");
	        Logger.log(testName, "read content from file /usr/root/final:"+out);
	        if(check) {
				if(!newContent.equals(out)) {
					String info = Logger.log(testName, "read file /usr/root/final, content is not "+newContent+": "+out);
					reportFailure(info);
				}
			}
	        inputStream.close();
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "read file /usr/root/final failed!");
				reportFailure(info);
			}
		}
		
		try {
			fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static ArrayList<String> reportFailure(String info) {
		if(failSH != null) {
			String path = failSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" \""+info+"\"", workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
	
}
