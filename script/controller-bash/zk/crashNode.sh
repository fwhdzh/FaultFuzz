SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $SCRIPT_DIR/configuration.sh



function crash {
        nodeName=$(docker network inspect ${CRASHFUZZ_NETWORK_NAME} | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        echo "crash $nodeName"
        docker exec -t $nodeName /bin/bash -ic "jps"
        docker exec -t $nodeName /bin/bash -ic "pkill -9 -u root && jps"
	docker kill $nodeName
}

crash $1