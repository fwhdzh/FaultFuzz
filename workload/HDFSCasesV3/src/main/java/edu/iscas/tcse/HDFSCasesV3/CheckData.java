package edu.iscas.tcse.HDFSCasesV3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CheckData {
	static String failSH;
	static String md5SH;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File input = new File(args[0]);
		if(!input.exists()) {
			System.out.println("CheckData, the file with blk file names does not exist");
			return;
		}
		int replica = Integer.parseInt(args[1]);
		md5SH = args[2];
		failSH = args[3];

//		List<File> blkFiles = new ArrayList<File>();
		Map<String, HashSet<String>> fileNameToAbsName = new HashMap<String, HashSet<String>>();
		try {
			FileReader fileReader;
			fileReader = new FileReader(input);

			BufferedReader br = new BufferedReader(fileReader);
            String lineContent = null;
            while((lineContent = br.readLine()) != null){
            	File file = new File(lineContent.trim());
            	if(file.exists()) {
//            		blkFiles.add(file);
            		HashSet<String> files = fileNameToAbsName.computeIfAbsent(file.getName(), k -> new HashSet<>());
            		files.add(file.getAbsolutePath());
            	}
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String key:fileNameToAbsName.keySet()) {
			HashSet<String> blks = fileNameToAbsName.get(key);
			if(blks.size()<replica) {
				String info = "Block "+key+" has less than "+replica+" replicas:"+blks.size();
				reportFailure(info);
			}
			Map<String, Integer> md5ToSize = new HashMap<String, Integer>();
			for(String blk:blks) {
				String md5 = getMD5Sum(blk);
				int size = md5ToSize.computeIfAbsent(md5, k -> 0);
        		size++;
        		md5ToSize.put(md5, size);
			}
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
	public static String getMD5Sum(String file) {
		if(md5SH != null) {
			String path = md5SH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+file, workingDir).get(1);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
}
