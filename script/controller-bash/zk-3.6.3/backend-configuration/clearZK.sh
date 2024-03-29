OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker exec -t C1ZK1 /bin/bash -ic "cd /zookeeper-3.6.3 && rm -rf zkData"
docker cp $OWN_DIR/zkData C1ZK1:/zookeeper-3.6.3
docker exec -t C1ZK1 /bin/bash -ic "cd /zookeeper-3.6.3 && echo 1 > zkData/myid"

docker exec -t C1ZK2 /bin/bash -ic "cd /zookeeper-3.6.3 && rm -rf zkData"
docker cp $OWN_DIR/zkData C1ZK2:/zookeeper-3.6.3
docker exec -t C1ZK2 /bin/bash -ic "cd /zookeeper-3.6.3 && echo 2 > zkData/myid"

docker exec -t C1ZK3 /bin/bash -ic "cd /zookeeper-3.6.3 && rm -rf zkData"
docker cp $OWN_DIR/zkData C1ZK3:/zookeeper-3.6.3
docker exec -t C1ZK3 /bin/bash -ic "cd /zookeeper-3.6.3 && echo 3 > zkData/myid"

docker exec -t C1ZK4 /bin/bash -ic "cd /zookeeper-3.6.3 && rm -rf zkData"
docker cp $OWN_DIR/zkData C1ZK4:/zookeeper-3.6.3
docker exec -t C1ZK4 /bin/bash -ic "cd /zookeeper-3.6.3 && echo 4 > zkData/myid"

docker exec -t C1ZK5 /bin/bash -ic "cd /zookeeper-3.6.3 && rm -rf zkData"
docker cp $OWN_DIR/zkData C1ZK5:/zookeeper-3.6.3
docker exec -t C1ZK5 /bin/bash -ic "cd /zookeeper-3.6.3 && echo 5 > zkData/myid"
