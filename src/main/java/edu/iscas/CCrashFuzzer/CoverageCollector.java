package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoverageCollector {
	static byte[] virgin_bits,     /* Regions yet untouched by fuzzing */
                  virgin_tmout,    /* Bits we haven't seen in tmouts   */
                  virgin_crash;    /* Bits we haven't seen in crashes  */
	static byte[] trace_bits;//store covered bits in a run
	
    public int actualSize(){
		// return Conf.MAP_SIZE / 8 + (Conf.MAP_SIZE % 8 == 0 ? 0 : 1);
		return Conf.MAP_SIZE;
	}

	public CoverageCollector() {
		virgin_bits = new byte[actualSize()];
        Arrays.fill(virgin_bits, (byte)1);

		trace_bits = new byte[actualSize()];
		Arrays.fill(trace_bits, (byte)0);
	}
	/* Check if the current execution path brings anything new to the table.
	   Update virgin bits to reflect the finds. Returns 1 if the only change is
	   the hit-count for a particular tuple; 2 if there are new tuples seen. 
	   Updates the map, so subsequent calls will always return 0.
	   This function is called after every exec() on a fairly large buffer, so
	   it needs to be fast. We do this in 32-bit and 64-bit flavors. */

	//my return the new covered bits.
	public int has_new_bits() {
		int sum=0;
		int c=0;
		for(int i=0;i<virgin_bits.length;i++) {
			if(virgin_bits[i] == 1 && trace_bits[i] == 1) {
				sum++;
				virgin_bits[i] = (byte)0;
			}
			
//		  int newCov = (virgin_bits[i]&trace_bits[i]);
//		  c=newCov;
//		  while(c!=0) {
//			  c&=(c-1);
//			  sum++;
//		  }
//		  virgin_bits[i] = (byte) (newCov^virgin_bits[i]);
		}
		Stat.log("Covered "+sum+" new code blocks!!!!!!!!!!!!!!!!!!!");
		return sum;
	}
	
	public static int has_new_cov(byte[] virgin,byte[] traced) {
		int sum=0;
		int c=0;
		for(int i=0;i<virgin.length;i++) {
		  int newCov = (virgin[i]&traced[i]);
		  c=newCov;
		  while(c!=0) {
			  c&=(c-1);
			  sum++;
		  }
		  virgin[i] = (byte) (newCov^virgin[i]);
		}
		return sum;
	}
	
	/* Write bitmap to file. The bitmap is useful mostly for the secret
	   -B option, to focus a separate fuzzing session on a particular
	   interesting input without rediscovering all the others. */

	public void write_bitmap() {

	}
	
	/* Read bitmap from file. This is for the -B option again. */

	public void read_bitmap(String fname) {
		Arrays.fill(trace_bits, (byte)0);
		File dir = new File(fname);
		List<File> covFiles = new ArrayList<File>();
		findCovFileList(dir, covFiles);
		for(File f:covFiles) {
			try {
				byte[] data = new byte[Conf.MAP_SIZE];
				Arrays.fill(data, (byte)0);
				FileInputStream coverFileIn = new FileInputStream(f);
				coverFileIn.read(data);
				for(int i = 0; i< trace_bits.length; i++) {
					trace_bits[i] = (byte) (trace_bits[i] | data[i]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Stat.log("read_bitmap-Got "+coveredBlocks(trace_bits)+" covered blocks!");
	}
	
	public static int coveredBlocks(byte[] data) {
		int sum = 0;
		for(int i=0;i<data.length;i++) {
			sum += data[i];
		}
		return sum;
	}

	public static void findCovFileList(File dir, List<File> fileNames) {
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            return;
        }
        String[] files = dir.list();// 读取目录下的所有目录文件信息
        for (int i = 0; i < files.length; i++) {// 循环，添加文件名或回调自身
            File file = new File(dir, files[i]);
            if (file.isFile() && file.getName().equals("fuzzcov")) {// 如果文件
                fileNames.add(file);// 添加文件全路径名
            } else {// 如果是目录
            	findCovFileList(file, fileNames);// 回调自身继续查询
            }
        }
    }
	
	/* When we bump into a new path, we call this to see if the path appears
	   more "favorable" than any of the existing ones. The purpose of the
	   "favorables" is to have a minimal set of paths that trigger all the bits
	   seen in the bitmap so far, and focus on fuzzing them at the expense of
	   the rest.
	   The first step of the process is to maintain a list of top_rated[] entries
	   for every byte in the bitmap. We win that slot if there is no previous
	   contender, or if the contender has a more favorable speed x size factor. */

	public void update_bitmap_score(QueueEntry q) {

	}
	
	/* The second part of the mechanism discussed above is a routine that
	   goes over top_rated[] entries, and then sequentially grabs winners for
	   previously-unseen bytes (temp_v) and marks them as favored, at least
	   until the next run. The favored entries are given more air time during
	   all fuzzing steps. */

	public void cull_queue() {

	}

	/* Examine map coverage. Called once, for first test case. */

	public void check_map_coverage() {

	}
	
	/* Calculate case desirability score to adjust the length of havoc fuzzing.
	   A helper function for fuzz_one(). Maybe some of these constants should
	   go into config.h. */

	static int calculate_score(QueueEntry q) {
		return 100;
	}
}
