package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFileTest2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int MAP_SIZE = 1000;
		byte[] input_bits = new byte[MAP_SIZE];
		Arrays.fill(input_bits, (byte)0);
		File f = new File("fuzzcov");
		List<File> covFiles = new ArrayList<File>();
		Stat.log("#################read bit map"+covFiles.size());
			try {
				byte[] data = new byte[MAP_SIZE];
				FileInputStream coverFileIn = new FileInputStream(f);
				coverFileIn.read(data);
				Stat.log("##################Get "+CoverageCollector.coveredBlocks(data)+" covs");
				for(int i = 0; i< input_bits.length; i++) {
					input_bits[i] = (byte) (input_bits[i] | data[i]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("finally:"+CoverageCollector.coveredBlocks(input_bits));
	}

}
