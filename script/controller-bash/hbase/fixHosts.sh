docker cp hbhosts C1HM1:/etc/
docker exec -t C1HM1 /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1HM2:/etc/
docker exec -t C1HM2 /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1RS1:/etc/
docker exec -t C1RS1 /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1RS2:/etc/
docker exec -t C1RS2 /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1RS3:/etc/
docker exec -t C1RS3 /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1hb-hdfs:/etc/
docker exec -t C1hb-hdfs /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1hb-zk:/etc/
docker exec -t C1hb-zk /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"

docker cp hbhosts C1hb-cli:/etc/
docker exec -t C1hb-cli /bin/bash -ic "cat /etc/hbhosts >> /etc/hosts"
