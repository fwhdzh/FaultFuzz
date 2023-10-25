import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTime {

    public static void main(String[] args) throws InterruptedException {
        test();
    }
    public static void test() throws InterruptedException {
    	// finding the time before the operation is executed
        long start = System.currentTimeMillis();
        for (int i = 0; i <5; i++) {
           Thread.sleep(1000);
        }
        // finding the time after the operation is executed
        long end = System.currentTimeMillis();
        System.out.println(end + " ends");
        //finding the time difference and converting it into seconds
        float sec = (end - start) / 1000F; System.out.println(sec + " seconds");
    }
}
