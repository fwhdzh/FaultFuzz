package edu.iscas.CCrashFuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFileTest3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int MAP_SIZE = 1000;
		byte[] input_bits = new byte[MAP_SIZE];
		Arrays.fill(input_bits, (byte)1);
		File f = new File("fuzzcov");
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(input_bits);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
			try {
				byte[] data = new byte[MAP_SIZE];
				FileInputStream in = new FileInputStream(f);
				in.read(data);
				Stat.log("##################Get "+CoverageCollector.coveredBlocks(data)+" covs");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("finally:"+CoverageCollector.coveredBlocks(input_bits));
	}

}
