docker restart C1ZK1
#sleep 10
docker restart C1ZK2
#sleep 10
docker restart C1ZK3
docker restart C1ZK4
docker restart C1ZK5

docker exec -t C1ZK1 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1ZK2 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1ZK3 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1ZK4 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1ZK5 /bin/bash -ic "jps && pkill -9 -u root && jps"
