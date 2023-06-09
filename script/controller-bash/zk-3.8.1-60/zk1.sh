# docker exec -t C1ZK1 /bin/bash -ic ". /home/gaoyu/evaluation/zk-3.8.1/bin/zkServer.sh start "
# docker exec -t C1ZK2 /bin/bash -ic ". /home/gaoyu/evaluation/zk-3.8.1/bin/zkServer.sh start "
# docker exec -t C1ZK3 /bin/bash -ic ". /home/gaoyu/evaluation/zk-3.8.1/bin/zkServer.sh start "
# docker exec -t C1ZK4 /bin/bash -ic ". /home/gaoyu/evaluation/zk-3.8.1/bin/zkServer.sh start "
# docker exec -t C1ZK5 /bin/bash -ic ". /home/gaoyu/evaluation/zk-3.8.1/bin/zkServer.sh start "

# java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.GetLeader "172.40.0.2:11181,172.40.0.3:11181,172.40.0.4:11181,172.40.0.5:11181,172.40.0.6:11181" $workdir/failTest.sh 10

START_TIME=`date +%s`

workdir=$(pwd)

#start ZooKeeper cluster
docker exec -t C1ZK1 /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'
docker exec -t C1ZK2 /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'
docker exec -t C1ZK3 /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'
docker exec -t C1ZK4 /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'
docker exec -t C1ZK5 /bin/bash -ic 'cd /home/gaoyu/evaluation/zk-3.8.1/ && bin/zkServer.sh start'

#make sure leader node is online
java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.GetLeader "172.40.0.2:11181,172.40.0.3:11181,172.40.0.4:11181,172.40.0.5:11181,172.40.0.6:11181" $workdir/failTest.sh 10

# export PHOS_OPTS="-Xbootclasspath/a:FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=false"

# echo "$(date "+%Y-%m-%d %H:%M:%S") FAV: start normal test (test1)!"

# #get alive nodes in the cluster
# servers=$(sh aliveServers.sh)
# echo $servers

# servers=$(echo "$servers" | sed 's/ZK5/ZK6/g')
# servers=$(echo "$servers" | sed 's/ZK4/ZK5/g')
# servers=$(echo "$servers" | sed 's/ZK3/ZK4/g')
# servers=$(echo "$servers" | sed 's/ZK2/ZK3/g')
# servers=$(echo "$servers" | sed 's/ZK1/ZK2/g')
# servers=$(echo "$servers" | sed 's/C1ZK/172\.40\.0\./g')
# echo $servers

# #run workload
# java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZK1Cli "$servers" check nullcrash nullstart $workdir/failTest.sh

# echo "exec edu.iscas.tcse.ZKCases.ZK1Cli finished!"

# servers=$(sh aliveServers.sh)
# echo $servers

# servers=$(echo "$servers" | sed 's/ZK5/ZK6/g')
# servers=$(echo "$servers" | sed 's/ZK4/ZK5/g')
# servers=$(echo "$servers" | sed 's/ZK3/ZK4/g')
# servers=$(echo "$servers" | sed 's/ZK2/ZK3/g')
# servers=$(echo "$servers" | sed 's/ZK1/ZK2/g')
# servers=$(echo "$servers" | sed 's/C1ZK/172\.40\.0\./g')
# echo $servers
# java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZK1Cli2 "$servers" check nullcrash nullstart $workdir/failTest.sh

# echo "exec edu.iscas.tcse.ZKCases.ZK1Cli2 finished!"

# END_TIME=`date +%s`
# EXECUTING_TIME=`expr $END_TIME - $START_TIME`
# echo $EXECUTING_TIME