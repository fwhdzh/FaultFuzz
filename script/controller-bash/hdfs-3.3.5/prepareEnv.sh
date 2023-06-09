OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

sh $OWN_DIR/restartAllDockers.sh
docker exec -t C1NN /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Master1 /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Master2 /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Slave1 /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Slave2 /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Slave3 /bin/bash -ic '/etc/init.d/ssh start'
docker exec -t C1Slave4 /bin/bash -ic '/etc/init.d/ssh start'
sh $OWN_DIR/fixHosts.sh

#sh stopHDFS.sh
#sh startHDFS.sh
#docker exec -t C1hd-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hd-zk-3.6.3/ && bin/zkCli.sh -server localhost:11181 deleteall /hadoop'
docker exec -t C1hd-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hd-zk-3.6.3/ && rm -rf logs/* && rm -rf zkData1 && rm -rf zkData2 && rm -rf zkData3 && cp -r zkData1-init zkData1 && cp -r zkData2-init zkData2 &&  cp -r zkData3-init zkData3'
sh $OWN_DIR/startZK.sh
sleep 10s

#sh clearRst.sh
sh $OWN_DIR/clearDockerRst.sh
sh $OWN_DIR/initHDFS.sh
sh $OWN_DIR/clearLogs.sh
#sh clearHB.sh

sh $OWN_DIR/clean-error-file-in-cluster.sh

