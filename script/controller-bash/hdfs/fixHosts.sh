docker cp hdhosts C1NN:/etc/
docker exec -t C1NN /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1RM:/etc/
docker exec -t C1RM /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Master1:/etc/
docker exec -t C1Master1 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Master2:/etc/
docker exec -t C1Master2 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Slave1:/etc/
docker exec -t C1Slave1 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Slave2:/etc/
docker exec -t C1Slave2 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Slave3:/etc/
docker exec -t C1Slave3 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1Slave4:/etc/
docker exec -t C1Slave4 /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1HS:/etc/
docker exec -t C1HS /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1WPS:/etc/
docker exec -t C1WPS /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

docker cp hdhosts C1hd-zk:/etc/
docker exec -t C1hd-zk /bin/bash -ic "cat /etc/hdhosts >> /etc/hosts"

