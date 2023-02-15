package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.ArrayList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;

public class MRProtocols {
    public static List<String> protocols = new ArrayList<String>();
    public static List<String> mapredRpcs = new ArrayList<String>();
    public static List<String> yarnRpcs = new ArrayList<String>();
    static {//13 protocols
        //yarn_server_common_service_protos
        protocols.add("org/apache/hadoop/mapreduce/v2/api/MRClientProtocolPB"); //client -> MRAM
        protocols.add("org/apache/hadoop/mapreduce/v2/api/HSClientProtocolPB");
        protocols.add("org/apache/hadoop/mapreduce/v2/api/HSAdminRefreshProtocolPB");
        // org/apache/hadoop/mapred/TaskUmbilicalProtocol //task child -> AM
        //client: record; TaskUmbilicalProtocol.invoke(); taint
        //server: TaskAttemptListenerImpl invoke() {taint,  ... ,  record, return;}
        // org/apache/hadoop/mapreduce/protocol/ClientProtocol  // JobClient -> JobTracker
        //some connections are not setup through RPCs, e.g., shuffler fetcher connections.
        mapredRpcs.add("org/apache/hadoop/mapred/TaskUmbilicalProtocol");
        mapredRpcs.add("org/apache/hadoop/mapreduce/protocol/ClientProtocol");

        yarnRpcs.add("org/apache/hadoop/yarn/api/ContainerManagementProtocolPB");
        yarnRpcs.add("org/apache/hadoop/yarn/api/ApplicationMasterProtocolPB");
        yarnRpcs.add("org/apache/hadoop/yarn/service/ClientAMProtocolPB");
//        protocols.add("org/apache/hadoop/yarn/api/ContainerManagementProtocolPB");//ApplicationMaster -> NM
//        protocols.add("org/apache/hadoop/yarn/api/ApplicationMasterProtocolPB");//ApplicationMaster -> RM
//        protocols.add("org/apache/hadoop/yarn/service/ClientAMProtocolPB"); //client -> ApplicationMaster
    }

    public static boolean isMRProtocol(String s) {
        for(String p : protocols) {
            if(p.equals(s) || ("L"+p+";").equals(s) || p.equals(s.replace(".", "/"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMRRPCs(String s) {
        for(String p : mapredRpcs) {
            if(p.equals(s) || ("L"+p+";").equals(s) || p.equals(s.replace(".", "/"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMRRPCs(String owner, String[] names) {
//    	if(owner.startsWith("org/apache/hadoop")
//                || owner.startsWith("Lorg/apache/hadoop")) {
//            //filter com/sun/proxy/$Proxy73 etc.
//    		for(String name:names) {
//        		for(String p : mapredRpcs) {
//                    if(p.equals(name) || ("L"+p+";").equals(name) || p.equals(name.replace(".", "/"))) {
//                        return true;
//                    }
//                }
//        	}
//    	}
    	for(String name:names) {
    		for(String p : mapredRpcs) {
                if(p.equals(name) || ("L"+p+";").equals(name) || p.equals(name.replace(".", "/"))) {
                    return true;
                }
            }
    	}
        return false;
    }

    public static boolean isYarnRPCs(String s) {
        for(String p : yarnRpcs) {
            if(p.equals(s) || ("L"+p+";").equals(s) || p.equals(s.replace(".", "/"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isYarnRPCs(String owner, String[] names) {
    	if(owner.startsWith("org/apache/hadoop")
                || owner.startsWith("Lorg/apache/hadoop")) {
            //filter com/sun/proxy/$Proxy73 etc.
    		for(String name:names) {
        		for(String p : yarnRpcs) {
                    if(p.equals(name) || ("L"+p+";").equals(name) || p.equals(name.replace(".", "/"))) {
                        return true;
                    }
                }
        	}
    	}
        return false;
    }

    public static boolean isServerSideProtocolImpl(String owner, String supername, String[] interfaces) {
        if(owner.startsWith("org/apache/hadoop")
                || owner.startsWith("Lorg/apache/hadoop")) {
            //filter com/sun/proxy/$Proxy73 etc.
            for(String i: interfaces) {
                if(isMRProtocol(i)) {
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
        String[] contents = desc.split(";");
        //approach 1:
        return contents[3].substring(1);
        //approach 2:
        //find element that contains the RequestProto
    }

    //desc mush be a rpc call desc wrapped by phosphor
    public static String getResponseProtoInternalTypeFromDesc(String desc) {
        String[] contents = desc.split(";");
        return contents[6].substring(1);
    }
}
