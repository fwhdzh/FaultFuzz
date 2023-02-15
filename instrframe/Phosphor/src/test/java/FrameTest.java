import java.io.File;

import edu.columbia.cs.psl.phosphor.Configuration;

public class FrameTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "init";
		if(s.isEmpty()) {
			s = "empty";
		} else {
			s = s + "new";
		}
		System.out.println(s);
	}

}
