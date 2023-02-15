package edu.iscas.tcse.favtrigger.instrumenter.yarn;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.ArrayList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;

public class YarnProtocols {

    public static List<String> protocols = new ArrayList<String>();
    static {//13 protocols
        //yarn_server_common_service_protos
        protocols.add("org/apache/hadoop/yarn/server/api/ResourceTrackerPB"); //NM -> RM
        protocols.add("org/apache/hadoop/yarn/server/api/SCMUploaderProtocolPB");//NM -> SharedCacheManager
        protocols.add("org/apache/hadoop/yarn/server/api/DistributedSchedulingAMProtocolPB"); //extends ApplicationMasterProtocol
        protocols.add("org/apache/hadoop/yarn/server/api/CollectorNodemanagerProtocolPB");//TimelineCollectorManager -> NM

        //yarn_server_nodemanager_service_protos
        protocols.add("org/apache/hadoop/yarn/server/nodemanager/api/LocalizationProtocolPB");//NM -> NM

        //yarn_service_protos
        protocols.add("org/apache/hadoop/yarn/server/api/SCMAdminProtocolPB");//administrators -> SharedCacheManager
        protocols.add("org/apache/hadoop/yarn/api/ClientSCMProtocolPB");//client -> SharedCacheManager
        protocols.add("org/apache/hadoop/yarn/api/ContainerManagementProtocolPB");//ApplicationMaster -> NM
        protocols.add("org/apache/hadoop/yarn/api/ApplicationHistoryProtocolPB");//client -> ApplicationHistoryServer
        protocols.add("org/apache/hadoop/yarn/api/ApplicationMasterProtocolPB");//ApplicationMaster -> RM
        protocols.add("org/apache/hadoop/yarn/api/ApplicationClientProtocolPB");//client -> ResourceManager

        //server/yarn_server_resourcemanager_service_protos
        protocols.add("org/apache/hadoop/yarn/server/api/ResourceManagerAdministrationProtocolPB");

        protocols.add("org/apache/hadoop/yarn/service/ClientAMProtocolPB"); //client -> ApplicationMaster

        protocols.add("org/apache/hadoop/ha/protocolPB/HAServiceProtocolPB");
        protocols.add("org/apache/hadoop/ha/protocolPB/ZKFCProtocolPB");
//
//        protocols.add("org/apache/hadoop/ha/protocolPB");
    }

    public static boolean isYarnProtocol(String s) {
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
                if(isYarnProtocol(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRpcCallDesc(String desc) {
        String[] contents = desc.split(";");
        if(Configuration.IS_THIRD_PARTY_PROTO) {
        	return (desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/apache/hadoop/thirdparty/protobuf/RpcController;Ledu/columbia/cs/psl/phosphor/runtime/Taint;")
                    && contents.length == 8
                    && desc.contains("RequestProto;")
                    && desc.contains("ResponseProto;")
                    && desc.endsWith("Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;"));
        } else {
        	return (desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lcom/google/protobuf/RpcController;Ledu/columbia/cs/psl/phosphor/runtime/Taint;")
                    && contents.length == 8
                    && desc.contains("RequestProto;")
                    && desc.contains("ResponseProto;")
                    && desc.endsWith("Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;"));
        }
    }

    //there is a very small chance to report a wrong rpc call
    public static boolean isServerSideRpcCall(String owner, String name, String desc, String supername, String[] interfaces) {
        return (isServerSideProtocolImpl(owner, supername, interfaces) && isRpcCallDesc(desc));
    }

  //desc mush be a rpc call desc wrapped by phosphor
    public static String getRequestProtoInternalTypeFromDesc(String desc) {
    	//Response function(RpcController controller, Request request)
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
