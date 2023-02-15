package edu.iscas.tcse.favtrigger;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.iscas.tcse.favtrigger.taint.Source;
import edu.iscas.tcse.favtrigger.taint.SpecialLabel;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;

public class TestTraceReader {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String label = "IP*PRC*-9223372036854581531*java/net/SocketInputStream*socketRead0*(Ljava/io/FileDescriptor;[BIII)I*NATIVERTN*worker3/172.18.0.6&&30";
        String[] secs = label.trim().split("\\*");
        System.out.println(secs.length);
        String link = "FAVMSG:worker1:/1.2.2.4&&234";
        System.out.println(link.replace("FAVMSG:", ""));
    }

}
