OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/configuration.sh

function prepareCrash {
        nodeName=$(docker network inspect ${CRASHFUZZ_NETWORK_NAME} | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        echo "store network information on $nodeName.."
        echo "docker exec -t $nodeName /bin/bash -ic \"iptables-save > /observer/iptables-rules\""
        docker exec -t $nodeName /bin/bash -ic "iptables-save > /observer/iptables-rules"
}

function crash {
        nodeName=$(docker network inspect ${CRASHFUZZ_NETWORK_NAME} | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        echo "crash $nodeName"
        docker exec -t $nodeName /bin/bash -ic "jps"
        docker exec -t $nodeName /bin/bash -ic "pkill -9 -u root && jps"
	docker kill $nodeName
}

prepareCrash $1
crash $1