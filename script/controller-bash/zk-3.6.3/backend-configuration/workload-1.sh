# docker exec -t C1ZK1 /bin/bash -ic ". /zookeeper-3.6.3/bin/zkServer.sh start "
# docker exec -t C1ZK2 /bin/bash -ic ". /zookeeper-3.6.3/bin/zkServer.sh start "
# docker exec -t C1ZK3 /bin/bash -ic ". /zookeeper-3.6.3/bin/zkServer.sh start "
# docker exec -t C1ZK4 /bin/bash -ic ". /zookeeper-3.6.3/bin/zkServer.sh start "
# docker exec -t C1ZK5 /bin/bash -ic ". /zookeeper-3.6.3/bin/zkServer.sh start "

# java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.GetLeader "172.30.0.2:11181,172.30.0.3:11181,172.30.0.4:11181,172.30.0.5:11181,172.30.0.6:11181" $workdir/failTest.sh 10

START_TIME=`date +%s`

workdir=$(pwd)

#start ZooKeeper cluster
docker exec -t C1ZK1 /bin/bash -ic 'cd /zookeeper-3.6.3/ && bin/zkServer.sh start'
docker exec -t C1ZK2 /bin/bash -ic 'cd /zookeeper-3.6.3/ && bin/zkServer.sh start'
docker exec -t C1ZK3 /bin/bash -ic 'cd /zookeeper-3.6.3/ && bin/zkServer.sh start'
docker exec -t C1ZK4 /bin/bash -ic 'cd /zookeeper-3.6.3/ && bin/zkServer.sh start'
docker exec -t C1ZK5 /bin/bash -ic 'cd /zookeeper-3.6.3/ && bin/zkServer.sh start'

#make sure leader node is online
java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.GetLeader "172.30.0.2:11181,172.30.0.3:11181,172.30.0.4:11181,172.30.0.5:11181,172.30.0.6:11181" $workdir/failTest.sh 10

# export PHOS_OPTS="-Xbootclasspath/a:FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false"

echo "$(date "+%Y-%m-%d %H:%M:%S") FAV: start normal test (test1)!"

#get alive nodes in the cluster
servers=$(sh aliveServers.sh)
echo $servers
#run workload
java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZK1Cli "$servers" check nullcrash nullstart $workdir/failTest.sh

echo "exec edu.iscas.tcse.ZKCases.ZK1Cli finished!"

servers=$(sh aliveServers.sh)
echo $servers
java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZK1Cli2 "$servers" check nullcrash nullstart $workdir/failTest.sh

echo "exec edu.iscas.tcse.ZKCases.ZK1Cli2 finished!"

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME