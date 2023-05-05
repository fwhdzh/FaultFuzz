package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.List;

public class Network {

    public static class NetworkPath {
        public String src;
        public String dst;
        public String toString() {
            return src + " -> " + dst;
        }

        private NetworkPath() {
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (!(obj instanceof NetworkPath)) {
                return false;
            }
            NetworkPath other = (NetworkPath) obj;
            if (src.equals(other.src) && dst.equals(other.dst)) {
                result = true;
            } else {
                result = false;
            }
            return result;
        }
    }

    // public List<NetworkPath> edges;

    public List<NetworkPath> connectedPath = new ArrayList<>();
    public List<NetworkPath> disconnectedPath = new ArrayList<>();

    public Network(List<String> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                NetworkPath path = new NetworkPath();
                path.src = nodes.get(i);
                path.dst = nodes.get(j);
                connectedPath.add(path);
            }
        }
    } 

    public void disconnect(String src, String dst) {
        NetworkPath path = getPath(src, dst);
        connectedPath.remove(path);
        disconnectedPath.add(path);
    }

    public void connect(String src, String dst) {
        NetworkPath path = getPath(src, dst);
        disconnectedPath.remove(path);
        connectedPath.add(path);
    }

    public boolean isConnected(String src, String dst) {
        NetworkPath path = getPath(src, dst);
        return connectedPath.contains(path);
    }

    private NetworkPath getPath(String src, String dst) {
        NetworkPath path = new NetworkPath();
        path.src = src;
        path.dst = dst;
        return path;
    }

    public static Network constructNetworkFromMaxDOwnNodes(List<MaxDownNodes> cluster) {
        List<String> nodes = new ArrayList<>();
        for (MaxDownNodes subCluster : cluster) {
            nodes.addAll(subCluster.aliveGroup);
            nodes.addAll(subCluster.deadGroup);
        }
        Network result = new Network(nodes);
        return result;
    }
}
