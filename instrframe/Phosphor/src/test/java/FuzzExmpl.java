
public class FuzzExmpl {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		openRegion();
	}

	public static void openRegion() {
		int a = 3;
		String s1 = "s";
		String s2 = null;
		if(((a <5) && s1.equals("")) || s2 == null) {
			a = 0;
			System.out.println("a>5");
		} else {
//			int b = 8;
			System.out.println("a <= 5;");
		}
		System.out.println("end"+a);
	}
}
