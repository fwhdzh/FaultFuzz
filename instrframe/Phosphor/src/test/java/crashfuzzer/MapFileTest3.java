package crashfuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.iscas.tcse.favtrigger.instrumenter.CoverageMap;

public class MapFileTest3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int MAP_SIZE = 1000;
//		int size = MAP_SIZE/ 8 + (MAP_SIZE % 8 == 0 ? 0 : 1);
		int size = MAP_SIZE;
		byte[] input_bits = new byte[size];
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
				byte[] data = new byte[size];
				FileInputStream coverFileIn = new FileInputStream(f);
				int rst = coverFileIn.read(data);
				System.out.println("##################Get "+rst+" "+CoverageMap.coveredBlocks(data)+" covs|"+CoverageMap.coveredBlocks2(data));
				for(int i = 0; i< input_bits.length; i++) {
					input_bits[i] = (byte) (input_bits[i] | data[i]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		System.out.println("finally:"+CoverageMap.coveredBlocks(input_bits));
	}

}
