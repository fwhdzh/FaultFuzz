OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/configuration.sh

sourceIP=$1
destIP=$2

function networkDisconnect {
        nodeName=$(docker network inspect ${CRASHFUZZ_NETWORK_NAME} | grep -B 5 "$sourceIP" | grep Name | awk -F"\"" '{print $4}')
        echo "disconnect the network from $sourceIP to $destIP on $nodeName"
        echo "docker exec -t $nodeName /bin/bash -ic \"iptables -A OUTPUT -d $destIP -j DROP\""
        docker exec -t $nodeName /bin/bash -ic "iptables -A OUTPUT -d $destIP -j DROP"
}

networkDisconnect