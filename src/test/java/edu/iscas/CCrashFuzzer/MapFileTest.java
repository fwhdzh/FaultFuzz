package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFileTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BitArray trace_map = new BitArray(10);
		trace_map.setBit(0, true);
		trace_map.setBit(5, true);
		trace_map.setBit(11, true);
		System.out.println("Created covered:"+CoverageCollector.coveredBlocks(trace_map.data));
		try {
			FileOutputStream coverFileOut = new FileOutputStream("MapFileTest");
//			FileUtils.writeByteArrayToFile(new File(""), trace_map.data);
			if(coverFileOut != null) {
				coverFileOut.write(trace_map.data, 0, trace_map.data.length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		byte[] input_bits = new byte[10/ 8 + (10 % 8 == 0 ? 0 : 1)];
		Arrays.fill(input_bits, (byte)0);
		File f = new File("MapFileTest");
		List<File> covFiles = new ArrayList<File>();
		Stat.log("#################read bit map"+covFiles.size());
			try {
				byte[] data = new byte[10/ 8 + (10 % 8 == 0 ? 0 : 1)];
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
