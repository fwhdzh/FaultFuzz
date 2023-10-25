#checker.sh [alive_ip1, alive_ip2, alive_ip3] [dead_ip1, dead_ip2]
function checkAlive(){
  node=$1
  character=$2
  workerRst=$(docker exec -t $node /bin/bash -ic 'jps')
  result=$(echo $workerRst | grep "${character}")
  if [[ "$result" == "" ]]; then
        echo "jps $node: $workerRst"
        sh failTest.sh "${node} ${character} was not started"
  fi
}



aliveNodes=""
deadNodes=""

if [[ "$1" != "NULL" ]]; then
aliveNodes=$1
fi

if [[ "$2" != "NULL" ]]; then
deadNodes=$2
fi

alivearr=(${aliveNodes//,/ })

for s in ${alivearr[@]}
do
    echo "alive: $s"
    case $s in
      172.40.0.2)
        checkAlive "C1ZK1" "QuorumPeerMain"
        ;;
      172.40.0.3)
        checkAlive "C1ZK2" "QuorumPeerMain"
        ;;
      172.40.0.4)
        checkAlive "C1ZK3" "QuorumPeerMain"
        ;;
      172.40.0.5)
        checkAlive "C1ZK4" "QuorumPeerMain"
        ;;
      172.40.0.6)
        checkAlive "C1ZK5" "QuorumPeerMain"
        ;;
    esac
done

deadarr=(${deadNodes//,/ })

for s in ${deadarr[@]}
do
    echo "dead: $s" 
done

sh jpsCluster.sh

workdir=$(pwd)

connectString=$(sh aliveServers.sh)
java -cp FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZKChecker "$connectString" $workdir/failTest.sh

sh checkException.sh $3

#sh zk1checkData.sh

jpsAll=$( sh jpsCluster.sh)
hasRunJar=$(echo $jpsAll| grep "JarBootstrapMain")
if [[ "$hasRunJar" != "" ]]; then
    sh failTest.sh "The client was not exit!"

fi
