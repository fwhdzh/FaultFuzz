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

public class FailNameNode {
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
		String testName = "FailNameNode";
		
		Logger.log(testName, " start .................");
		Logger.log(testName, " going get distributed filesystem ..............");
		FileSystem fs = HDFSUtil.getDFSFileSystem();
		if(fs == null) {
			Logger.log(testName, " cannot get the distributed file system.");
			return;
		}

		long curTime = 0;
		FSDataOutputStream fsDataOutputStream = null;
		try {
			fsDataOutputStream = fs.create(new Path("/usr/root/test"));
			Logger.log(testName, "created file /usr/root/test:");
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "create file /usr/root/test failed!");
				reportFailure(info);
			}
		}

		if(fsDataOutputStream == null) {
			String info = Logger.log(testName, "create file /usr/root/test return null out stream, return!");
			reportFailure(info);
			return;
		}
		
		stopActiveNameNode();
		Logger.log(testName, " stop active name node.");
		
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
			curTime = System.currentTimeMillis();
			bufferedWriter.write(String.valueOf(curTime));
//	        bufferedWriter.newLine();
			fsDataOutputStream.hflush();
	        bufferedWriter.close();
	        Logger.log(testName, "write current time millis "+curTime+" to file /usr/root/test:");
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "write to file /usr/root/test failed!");
				reportFailure(info);
			}
		}
		
		String newContent = String.valueOf(curTime);
		try {
			FSDataInputStream inputStream = fs.open(new Path("/usr/root/test"));
			//Classical input stream usage
	        String out= IOUtils.toString(inputStream, "UTF-8");
	        Logger.log(testName, "read content from file /usr/root/test:"+out);
	        if(check) {
				if(!newContent.equals(out)) {
					String info = Logger.log(testName, "read file /usr/root/test, content is not "+newContent);
					reportFailure(info);
				}
			}
	        inputStream.close();
		} catch (IllegalArgumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "read file /usr/root/test failed!");
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
	public static ArrayList<String> stopActiveNameNode() {
		if(stopNodeSH != null) {
			String path = stopNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
}
