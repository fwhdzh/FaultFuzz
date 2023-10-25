OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

#checker.sh [alive_ip1, alive_ip2, alive_ip3] [dead_ip1, dead_ip2]
function checkAlive(){
  node=$1
  character=$2
  workerRst=$(docker exec -t $node /bin/bash -ic 'jps')
  result=$(echo $workerRst | grep "${character}")
  if [[ "$result" == "" ]]; then
        echo "jps $node: $workerRst"
        sh $OWN_DIR/failTest.sh "${node} ${character} was not started"
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

sh $OWN_DIR/jpsCluster.sh

workdir=$(pwd)

connectString=$(sh $OWN_DIR/aliveServers.sh)

connectString=$(echo "$connectString" | sed 's/ZK5/ZK6/g')
connectString=$(echo "$connectString" | sed 's/ZK4/ZK5/g')
connectString=$(echo "$connectString" | sed 's/ZK3/ZK4/g')
connectString=$(echo "$connectString" | sed 's/ZK2/ZK3/g')
connectString=$(echo "$connectString" | sed 's/ZK1/ZK2/g')
connectString=$(echo "$connectString" | sed 's/C1ZK/172\.40\.0\./g')
echo $connectString

# java -cp $OWN_DIR/ZKCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.ZKCases.ZKChecker "$connectString" $workdir/failTest.sh

sh $OWN_DIR/checkException.sh $3

#sh zk1checkData.sh

jpsAll=$( sh $OWN_DIR/jpsCluster.sh)
hasRunJar=$(echo $jpsAll| grep "JarBootstrapMain")
if [[ "$hasRunJar" != "" ]]; then
    sh $OWN_DIR/failTest.sh "The client was not exit!"

fi
