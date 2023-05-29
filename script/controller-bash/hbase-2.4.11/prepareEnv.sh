OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "sh $OWN_DIR/restartAllDockers.sh"
sh $OWN_DIR/restartAllDockers.sh
echo "docker exec -t C1HM1 /bin/bash -ic '/etc/init.d/ssh start'"
docker exec -t C1HM1 /bin/bash -ic '/etc/init.d/ssh start'
echo "docker exec -t C1HM2 /bin/bash -ic '/etc/init.d/ssh start'"
docker exec -t C1HM2 /bin/bash -ic '/etc/init.d/ssh start'
echo "docker exec -t C1RS1 /bin/bash -ic '/etc/init.d/ssh start'"
docker exec -t C1RS1 /bin/bash -ic '/etc/init.d/ssh start'
echo "docker exec -t C1RS2 /bin/bash -ic '/etc/init.d/ssh start'"
docker exec -t C1RS2 /bin/bash -ic '/etc/init.d/ssh start'
echo "docker exec -t C1RS3 /bin/bash -ic '/etc/init.d/ssh start'"
docker exec -t C1RS3 /bin/bash -ic '/etc/init.d/ssh start'
echo "$OWN_DIR/fixHosts.sh"
sh $OWN_DIR/fixHosts.sh

#sh stopHDFS.sh
echo "docker restart C1hb-hdfs"
docker restart C1hb-hdfs
echo "docker exec -t C1hb-hdfs /bin/bash -ic \"jps && pkill -9 -u root && jps\""
docker exec -t C1hb-hdfs /bin/bash -ic "jps && pkill -9 -u root && jps"
echo "docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && rm -rf logs/*'"
docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && rm -rf logs/*'
echo "sh $OWN_DIR/startHDFS.sh"
sh $OWN_DIR/startHDFS.sh
echo "docker exec -t C1hb-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-zk-3.6.3/ && bin/zkCli.sh -server localhost:11181 deleteall /hbase'"
docker exec -t C1hb-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-zk-3.6.3/ && bin/zkCli.sh -server localhost:11181 deleteall /hbase'

#sh clearRst.sh
echo "sh $OWN_DIR/clearDockerRst.sh"
sh $OWN_DIR/clearDockerRst.sh
echo "sh $OWN_DIR/clearHB.sh"
sh $OWN_DIR/clearHB.sh

sh $OWN_DIR/clear-checker-process.sh
sh $OWN_DIR/clear-network-condition.sh

sh $OWN_DIR/copy-env-to-cluster.sh
