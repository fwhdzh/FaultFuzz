package edu.iscas.tcse.favtrigger.instrumenter.yarn;

import java.net.InetSocketAddress;

public class YarnInstrument {

    public static String storeYarnRpcClientSideSocket(Class protocol, InetSocketAddress address) {
        //note!!!! address could be an unresolved address, e.g., RM1:8031
        //The later one would produce null getAddress() result
        if(YarnProtocols.isYarnProtocol(protocol.getName())) {
            if(address == null) {
                System.out.println("!!!FAVTrigger storeYarnRpcClientSideSocket get null address");
            } else if (address.getAddress() == null) {
                System.out.println("!!!FAVTrigger storeYarnRpcClientSideSocket cannot resolve the address");
                return address.getHostName();
            }
            return address.getAddress().getHostAddress();
        } else {
            return null;
        }
    }
}
