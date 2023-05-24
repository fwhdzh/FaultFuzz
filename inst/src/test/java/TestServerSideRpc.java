import edu.iscas.tcse.favtrigger.instrumenter.hdfs.HDFSProtocols;

public class TestServerSideRpc {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String desc = "(Ledu/columbia/cs/psl/phosphor/runtime/Taint;"
				+ "Lorg/apache/hadoop/thirdparty/protobuf/RpcController;"
				+ "Ledu/columbia/cs/psl/phosphor/runtime/Taint;"
				+ "Lorg/apache/hadoop/hdfs/protocol/proto/ClientDatanodeProtocolProtos$DiskBalancerSettingRequestProto;"
				+ "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;Lorg/apache/hadoop/hdfs/protocol/proto/ClientDatanodeProtocolProtos$DiskBalancerSettingResponseProto;)"
				+ "Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;";
		System.out.println(HDFSProtocols.isRpcCallDesc(desc));
		
		String[] contents = desc.split(";");
		System.out.println(desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lcom/google/protobuf/RpcController;Ledu/columbia/cs/psl/phosphor/runtime/Taint;")
                );
		System.out.println(contents.length == 8);
		System.out.println(desc.contains("RequestProto;"));
		
		System.out.println(desc.contains("ResponseProto;"));
		System.out.println(desc.endsWith("Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;"));

	}

}
