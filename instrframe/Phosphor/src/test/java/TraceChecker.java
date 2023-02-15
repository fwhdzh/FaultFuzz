import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl;


public class TraceChecker {
	public static int _get_map_size() {
    	int MAP_SIZE_POW2 = 16;
    	int MAP_SIZE = (1 << MAP_SIZE_POW2);
    	return MAP_SIZE;
    }

	static byte[] trace_bits = new byte[_get_map_size()];
	static byte[] virgin_bits = new byte[_get_map_size()];//covered
	
	public static void test() {
		String x = "12342134123421309i4-102493i09fg                          123121";
		byte[] contents = x.getBytes();
		System.out.println((contents[0] > 0)?true:false);

		try {
			FileOutputStream out = new FileOutputStream("fuzzcov", false);
			out.write(x.getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		test();
//		args = new String[3];
//		args[0] = "fuzzcov";
//		args[1] = "cov";
//		args[2] = "virgincov";
		
        Arrays.fill(virgin_bits, (byte)0);

		Arrays.fill(trace_bits, (byte)0);
		
		// TODO Auto-generated method stub
		String trace_file = args[0];
		String trace_dir = args[1];
		String traced_file = args[2];
		
		//load virgin bits
		File virgin = new File(traced_file);
		int covored = 0;
		if(virgin.exists()) {
			try {
				FileInputStream tracedFileIn = new FileInputStream(virgin);
				tracedFileIn.read(virgin_bits);
				tracedFileIn.close();
				
				for(int i = 0; i< virgin_bits.length; i++) {
					if(virgin_bits[i] > 0) {
						covored++;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Got "+covored+" existing edges.");
		
		//load trace bits
		List<File> traces = new ArrayList<File>();
		loadTrace(traces, trace_dir, trace_file);
		System.out.println("Got "+traces.size()+" coverage files.");
		int edges = 0;
		for(File f:traces) {
			try {
				byte[] data = new byte[_get_map_size()];
				Arrays.fill(data, (byte)0);
				FileInputStream coverFileIn = new FileInputStream(f);
				coverFileIn.read(data);
				for(int i = 0; i< trace_bits.length; i++) {
					trace_bits[i] = (byte) Math.max(trace_bits[i], data[i]);
				}
				coverFileIn.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i = 0; i< trace_bits.length; i++) {
			if(trace_bits[i] > 0) {
				edges++;
			}
		}
		System.out.println("Got "+edges+" edges for this trace.");
		
		//check new bits
		int finds= 0;
		for(int i = 0; i< trace_bits.length; i++) {
			if(trace_bits[i]>0 && virgin_bits[i] ==0) {
				finds++;
				virgin_bits[i] = trace_bits[i];
				System.out.println("Got new edge:"+i+".");
			}
		}
		System.out.println("Got "+finds+" new edges.");
		
		int rst = 0;
		for(int i = 0; i< virgin_bits.length; i++) {
			if(virgin_bits[i] > 0) {
				rst++;
			}
		}
		System.out.println("Current covered edges is "+rst);
		
		//store virgin_bits
		try {
			FileOutputStream out = new FileOutputStream(virgin);
			out.write(virgin_bits);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<File> loadTrace(List<File> fileList, String trace_dir, String trace_file) {
        File file = new File(trace_dir);
        if(file.isFile() && file.getName().equals(trace_file)) {
        	fileList.add(file);
        	return fileList;
        }
        
        File[] files = file.listFiles();// 获取目录下的所有文件或文件夹
        if (files == null) {// 如果目录为空，直接退出
            return fileList;
        }
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile() && f.getName().equals(trace_file)) {
//            	System.out.println("Find cov file "+f.getAbsolutePath());
                fileList.add(f);
            } else if (f.isDirectory()) {
//                System.out.println(f.getAbsolutePath());
                loadTrace(fileList, f.getAbsolutePath(), trace_file);
            }
        }
        return fileList;
	}
}
