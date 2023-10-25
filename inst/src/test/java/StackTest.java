import java.io.IOException;

public class StackTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start");
		String x = "hello";
		
		try {
			test(x);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("tested exception");
		}
		
		System.out.println("end");
	}
	
	public static void test(String con2) throws IOException {
		if(con2 != null){
            throw new IOException(String.format("hello",
                    (con2 == null? "triple operators": con2)));
        }
	}

}
