function crash {
        nodeName=$(docker network inspect fav-hdfs | grep -B 5 "$1" | grep Name | awk -F"\"" '{print $4}')
        docker exec -t $nodeName /bin/bash -ic "jps"
        docker exec -t $nodeName /bin/bash -ic "pkill -9 -u root && jps"
	docker kill $nodeName
}

sh getZKInfo.sh
crash $1
#sleep 5
