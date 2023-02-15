
public class TestLoadClass {

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Class c = ClassLoader.getSystemClassLoader().loadClass("java.lang.String");
		System.out.println(c.getName());
		
	}

}
