SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $SCRIPT_DIR/cluster-info.sh

function crash {
        nodeName=$(docker network inspect "$clusterNetName" | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
#        docker exec -t $nodeName /bin/bash -ic "jps"
        docker exec -t $nodeName /bin/bash -ic "jps && pkill -9 -u root && jps"
	docker kill $nodeName
}

sh $SCRIPT_DIR/getZKInfo.sh
crash $1
sleep 5
