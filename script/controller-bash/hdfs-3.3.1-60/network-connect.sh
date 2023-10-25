OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

sourceIP=$1
destIP=$2

function networkConnect {
        nodeName=$(docker network inspect $clusterNetName | grep -B 5 "$sourceIP" | grep Name | awk -F"\"" '{print $4}')
        echo "disconnect the network from $sourceIP to $destIP on $nodeName"
        echo "docker exec -t $nodeName /bin/bash -ic \"iptables -D OUTPUT -d $destIP -j DROP\""
        docker exec -t $nodeName /bin/bash -ic "iptables -D OUTPUT -d $destIP -j DROP"
}

networkConnect