OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

function crash {
        nodeName=$(docker network inspect $clusterNetName | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        docker exec -t $nodeName /bin/bash -ic "jps"
        docker exec -t $nodeName /bin/bash -ic "pkill -9 -u root && jps"
	docker kill $nodeName
}

function prepareCrash {
        nodeName=$(docker network inspect $clusterNetName | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        echo "store network information on $nodeName.."
        echo "docker exec -t $nodeName /bin/bash -ic \"iptables-save > /home/gaoyu/iptables-rules\""
        docker exec -t $nodeName /bin/bash -ic "iptables-save > /home/gaoyu/iptables-rules"
}


sh getZKInfo.sh
prepareCrash $1
crash $1
#sleep 5
