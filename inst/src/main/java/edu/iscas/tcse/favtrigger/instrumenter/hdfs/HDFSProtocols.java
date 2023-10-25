package edu.iscas.tcse.favtrigger.instrumenter.hdfs;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.ArrayList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;

public class HDFSProtocols {
    public static List<String> protocols = new ArrayList<String>();
    static {//13 protocols
        //
        protocols.add("org/apache/hadoop/hdfs/protocolPB/AliasMapProtocolPB");//NN and DN
        protocols.add("org/apache/hadoop/hdfs/protocolPB/DatanodeLifelineProtocolPB");//DN to NN
        protocols.add("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolPB");//DN to NN
        protocols.add("org/apache/hadoop/hdfs/protocolPB/InterDatanodeProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/JournalProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/NamenodeProtocolPB"); //2NN to NN
        protocols.add("org/apache/hadoop/hdfs/qjournal/protocolPB/InterQJournalProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/qjournal/protocolPB/QJournalProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/ClientDatanodeProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/ClientNamenodeProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/ReconfigurationProtocolPB");
        protocols.add("org/apache/hadoop/hdfs/protocolPB/RouterAdminProtocolPB");

        protocols.add("org/apache/hadoop/ha/protocolPB/HAServiceProtocolPB");
        protocols.add("org/apache/hadoop/ha/protocolPB/ZKFCProtocolPB");
    }

    public static boolean isHDFSProtocol(String s) {
        for(String p : protocols) {
            if(p.equals(s) || ("L"+p+";").equals(s) || p.equals(s.replace(".", "/"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isServerSideProtocolImpl(String owner, String supername, String[] interfaces) {
        if(owner.startsWith("org/apache/hadoop")
                || owner.startsWith("Lorg/apache/hadoop")) {
            //filter com/sun/proxy/$Proxy73 etc.
            for(String i: interfaces) {
                if(isHDFSProtocol(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRpcCallDesc(String desc) {
        String[] contents = desc.split(";");
        if(Configuration.IS_THIRD_PARTY_PROTO) {
        	return (desc.startsWith("(Lorg/apache/hadoop/thirdparty/protobuf/RpcController;")
                    && contents.length == 3
                    && desc.contains("RequestProto;")
                    && desc.endsWith("ResponseProto;"));
        } else {
        	return (desc.startsWith("(Lcom/google/protobuf/RpcController;")
                    && contents.length == 3
                    && desc.contains("RequestProto;")
                    && desc.endsWith("ResponseProto;"));
        }
    }

    //there is a very small chance to report a wrong rpc call
    public static boolean isServerSideRpcCall(String owner, String name, String desc, String supername, String[] interfaces) {
        return (isServerSideProtocolImpl(owner, supername, interfaces) && isRpcCallDesc(desc));
    }

  //desc mush be a rpc call desc wrapped by phosphor
    public static String getRequestProtoInternalTypeFromDesc(String desc) {
        String[] contents = desc.split(";");
        //approach 1:
        return contents[1].substring(1);
        //approach 2:
        //find element that contains the RequestProto
    }

    //desc mush be a rpc call desc wrapped by phosphor
    public static String getResponseProtoInternalTypeFromDesc(String desc) {
        String[] contents = desc.split(";");
        return contents[2].substring(2);
    }
}
