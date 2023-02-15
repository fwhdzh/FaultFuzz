import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class NewTaintTest {

	public static class Field implements java.io.Serializable {
		int v = 0;
		String s;
	}
	public static class Data implements java.io.Serializable {
		Field f;
		String[] arrays;
		byte[] bytes;
	}
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String x = Long.toString(System.currentTimeMillis());
		
		Field f = new Field();
		f.s = x;
		
		Data d = new Data();
		d.f = f;
		d.arrays = new String[2];
		d.arrays[0] = "test";
		d.arrays[1] = Long.toString(System.currentTimeMillis());
		d.bytes = x.getBytes();
		System.out.println(d.bytes.length);
		System.out.println(d.f.s);
		System.out.println(d.arrays[0]);
		System.out.println(d.arrays[1]);
		
		FileOutputStream out = new FileOutputStream("add-output@Test", false);
		ObjectOutputStream objOut=new ObjectOutputStream(out);
		objOut.writeObject(d);
		objOut.close();
		
		/*
		ObjectInputStream ois=new ObjectInputStream(new FileInputStream("./add-output@Test"));
		Data md = (Data) ois.readObject();
		System.out.println(md.f.s);
		System.out.println(md.arrays[0]);
		System.out.println(md.arrays[1]);
		*/
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		byteout.write(d.bytes);
		ByteArrayOutputStream dataout = new ByteArrayOutputStream();
		byteout.writeTo(dataout);
		test(dataout);
	}

	public static void test(ByteArrayOutputStream out) {
		invoke();
	}
	public static void invoke() {
		
	}
}
