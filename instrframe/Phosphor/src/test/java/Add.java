import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import edu.columbia.cs.psl.phosphor.runtime.Taint;

public class Add {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long fileName = System.currentTimeMillis();
		long x = System.currentTimeMillis();
		long y = System.currentTimeMillis();
		FileOutputStream out = new FileOutputStream("add-output@Test", false);
		File f = new File("add-output@Test2");
		//out.write(x.getBytes());
		//oout.write(x.getBytes());
		//oout.write(x.getBytes());
		//oout.close();
	}
	
}