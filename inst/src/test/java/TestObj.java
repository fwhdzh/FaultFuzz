
public class TestObj {
    public static void main(String[] args) throws ClassNotFoundException {
        // TODO Auto-generated method stub
        
    }
    public static interface TestFace {
    	public void test();
    	public static void test(TestFace o) {
    		o.hashCode();
    	}
    	public static int testF(TestFace o) {
    		return o.hashCode();
    	}
    }
}
