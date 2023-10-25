import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import edu.columbia.cs.psl.phosphor.runtime.Taint;

public class TestLabel {

    public static String str1 = "default";
    public static String str2;

	@Test
	public void test() throws UnknownHostException {
		str2 = "str2";
		int i = 1;
		int j = 2;
		int k = i+j;
		System.out.println(k);
		String[] str = new String[3];
        str[0] = "first";
        str[1] = "second";
        str[2] = "third";           
        //方法1
        String allStr2 = new String();
        allStr2 = str[0] + str[1] + str[2];
        System.out.println(allStr2);
	}

}
