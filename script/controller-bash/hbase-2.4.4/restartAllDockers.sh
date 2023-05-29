docker restart C1HM1
#sleep 10
docker restart C1HM2
#sleep 10
docker restart C1RS1
#sleep 10
docker restart C1RS2
#sleep 10
docker restart C1RS3

docker exec -t C1HM1 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1HM2 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1RS1 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1RS2 /bin/bash -ic "jps && pkill -9 -u root && jps"
docker exec -t C1RS3 /bin/bash -ic "jps && pkill -9 -u root && jps"
