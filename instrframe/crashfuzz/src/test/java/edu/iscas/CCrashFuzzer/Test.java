package edu.iscas.CCrashFuzzer;

import java.util.HashSet;
import java.util.Set;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] ips = new String[] {"127.0.0.1","127.0.0.1","127.0.0.2"};
		Set<String> ipList = new HashSet<String>();
		for(String ip:ips) {
			ipList.add(ip.trim());
		}
		System.out.println(ipList.toString());
		
		String[] ips2 = new String[] {"127.0.0.2","127.0.0.3","127.0.0.4"};
		Set<String> ipList2 = new HashSet<String>();
		for(String ip:ips2) {
			ipList2.add(ip.trim());
		}
		System.out.println(ipList2.toString());
		
		long x = Long.parseLong("8");
		System.out.println(x);
		
		double y = 1.99;
		int z = (int) y;
		System.out.println(z);
		
		byte[] d1 = new byte[65535];
		byte[] d2 = new byte[65535];
		for(int i = 0; i < 65535; i++) {
			d1[i] = (byte) 0x03;// 0011
			d2[i] = (byte) 0x05;// 0101
		}
		System.out.println("has new bits:"+has_new_bits(d1,d2));
		
		String call = "[org.apache.zookeeper.server.quorum.QuorumPacket.serialize$$PHOSPHORTAGGED(QuorumPacket.java:68), org.apache.jute.BinaryOutputArchive.writeRecord$$PHOSPHORTAGGED(BinaryOutputArchive.java:126), org.apache.zookeeper.server.quorum.LearnerHandler.sendPackets$$PHOSPHORTAGGED(LearnerHandler.java:356), org.apache.zookeeper.server.quorum.LearnerHandler.access$200$$PHOSPHORTAGGED(LearnerHandler.java:62), org.apache.zookeeper.server.quorum.LearnerHandler$1.run$$PHOSPHORTAGGED(LearnerHandler.java:753), org.apache.zookeeper.server.quorum.LearnerHandler$1.run(LearnerHandler.java)]";
		System.out.println(call.hashCode());
		
		byte[] virgin = new byte[1];
		byte[] trace = new byte[1];
		virgin[0] = (byte) 12;
		trace[0] = (byte) 6;
		System.out.println("has new bits:"+has_new_bits(virgin,trace));
		System.out.println("has new bits:"+virgin[0]);
		
		byte[] data = new byte[2];
		data[0] = 5; //0000 0101
		data[1] = 7; //0000 0111
		System.out.println("covered:"+coveredBlocks(data));
		System.out.println(getBit(data,15));
		byte[] rtn = setBit(data, 15, true);
		System.out.println(data[1]); //128+7 = 135
		
		System.out.println(getBit(data,15));
	}
	
	public static byte[] setBit(byte[]data, int pos, boolean b) {
    	int posByte = pos/8;
        int posBit = pos%8;
        byte blockMark = data[posByte];
        if (b) {
        	blockMark |= (1 << posBit);
        } else {
        	blockMark &= ~(1 << posBit);
        }
        data[posByte] = blockMark;
        return data;
    }

    public static boolean getBit(byte[]data, int pos) {
    	return (data[pos / 8] & (1 << (pos % 8))) != 0;
    }

	public static int has_new_bits(byte[] origin,byte[] traced) {
		int sum=0;
		int c=0;
		for(int i=0;i<origin.length;i++) {
		  c=origin[i]^(origin[i]|traced[i]);
		  while(c!=0) {
			  c&=(c-1);
			  sum++;
		  }
		}
		return sum;
	}
	
	public static int has_new_cov(byte[] virgin,byte[] traced) {
		int sum=0;
		int c=0;
		for(int i=0;i<virgin.length;i++) {
		  int newCov = (virgin[i]&traced[i]);
		  c=newCov;
		  while(c!=0) {
			  c&=(c-1);
			  sum++;
		  }
		  virgin[i] = (byte) (newCov^virgin[i]);
		}
		return sum;
	}
	
	public static int coveredBlocks(byte[] data) {
		int sum = 0;
		for(int i=0;i<data.length;i++) {
			  int c = (data[i]);
			  while(c!=0) {
				  c&=(c-1);
				  sum++;
			  }
			}
			return sum;
	}
}
