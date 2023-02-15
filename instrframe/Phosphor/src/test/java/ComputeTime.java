import java.util.ArrayList;
import java.util.Comparator;

import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class ComputeTime {

	public static class Data {
		public long TIMESTAMP;

		public Data(long num) {
			TIMESTAMP = num;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return Long.toString(TIMESTAMP);
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long t2 = 16605705989068560L;
		long t1 = 16605595518490020L;
		System.out.println(((float)(t2-t1))/1000000000f);
		String s = "[org.apache.hadoop.hbase.shaded.protobuf.generated.RegionServerStatusProtos$RegionServerStatusService$BlockingStub.regionServerReport$$PHOSPHORTAGGED(RegionServerStatusProtos.java), org.apache.hadoop.hbase.regionserver.HRegionServer.tryRegionServerReport$$PHOSPHORTAGGED(HRegionServer.java:1274), org.apache.hadoop.hbase.regionserver.HRegionServer.run$$PHOSPHORTAGGED(HRegionServer.java:1092), org.apache.hadoop.hbase.regionserver.HRegionServer.run(HRegionServer.java)]";
		String m = "[org.apache.hadoop.hbase.shaded.protobuf.generated.RegionServerStatusProtos$RegionServerStatusService$BlockingStub.regionServerReport$$PHOSPHORTAGGED(RegionServerStatusProtos.java), org.apache.hadoop.hbase.regionserver.HRegionServer.tryRegionServerReport$$PHOSPHORTAGGED(HRegionServer.java:1274), org.apache.hadoop.hbase.regionserver.HRegionServer.run$$PHOSPHORTAGGED(HRegionServer.java:1092), org.apache.hadoop.hbase.regionserver.HRegionServer.run(HRegionServer.java)]";
		System.out.println(s.hashCode());
		System.out.println(m.hashCode());
		String g = "[org.apache.hadoop.hbase.shaded.protobuf.generated.RegionServerStatusProtos$RegionServerStatusService$BlockingStub.reportRegionStateTransition$$PHOSPHORTAGGED(RegionServerStatusProtos.java), org.apache.hadoop.hbase.regionserver.HRegionServer.reportRegionStateTransition$$PHOSPHORTAGGED(HRegionServer.java:2445), org.apache.hadoop.hbase.regionserver.handler.UnassignRegionHandler.process$$PHOSPHORTAGGED(UnassignRegionHandler.java:133), org.apache.hadoop.hbase.executor.EventHandler.run$$PHOSPHORTAGGED(EventHandler.java:104), java.util.concurrent.ThreadPoolExecutor.runWorker$$PHOSPHORTAGGED(ThreadPoolExecutor.java:1149), java.util.concurrent.ThreadPoolExecutor$Worker.run$$PHOSPHORTAGGED(ThreadPoolExecutor.java:624), java.lang.Thread.run$$PHOSPHORTAGGED(Thread.java:748), java.lang.Thread.run(Thread.java)]";
        System.out.println(isHexNumberRex(""));
        ArrayList<String> content = new ArrayList<>();
        System.out.println(content.toString().contains(WaitToExec.cannotSchedule));
	}
	private static boolean isHexNumberRex(String str){
		String validate = "(?i)[0-9a-f]+";
		return str.matches(validate);
	}
}
