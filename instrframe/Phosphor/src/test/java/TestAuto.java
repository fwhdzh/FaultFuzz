import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestAuto {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        FileOutputStream out = new FileOutputStream("add-output@Test", false);
        String v = getValue();
        out.write(v.getBytes());
        out.close();
    }
    
    public static String getValue() {
        return "Hello, world!";
    }

}
