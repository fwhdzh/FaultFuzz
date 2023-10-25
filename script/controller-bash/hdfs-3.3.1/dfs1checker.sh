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
      172.42.0.2)
        checkAlive "C1NN" "NameNode"
        checkAlive "C1NN" "DFSZKFailoverController"
        ;;
      172.42.0.4)
        checkAlive "C1Master1" "NameNode"
        checkAlive "C1Master1" "DFSZKFailoverController"
        ;;
      172.42.0.6)
        checkAlive "C1Slave1" "DataNode"
        checkAlive "C1Slave1" "JournalNode"
        ;;
      172.42.0.7)
        checkAlive "C1Slave2" "DataNode"
        checkAlive "C1Slave2" "JournalNode"
        ;;
      172.42.0.8)
        checkAlive "C1Slave3" "DataNode"
        checkAlive "C1Slave3" "JournalNode"
        ;;
    esac
done

sh jpsCluster.sh

sh checkException.sh $3

sh getZKInfo.sh
<<'COMMENT'
jpsAll=$( sh jpsCluster.sh)
hasRunJar=$(echo $jpsAll| grep "JarBootstrapMain")
if [[ "$hasRunJar" != "" ]]; then
    sh failTest.sh "The client was not exit!"

fi

zkInfo=$( sh getZKInfo.sh )
hasFAVTable=$(echo $zkInfo | grep "FAVMyInfo")
if [[ "$hasFAVTable" != "" ]]; then
    sh failTest.sh "The FAVMyInfo node is still in ZK:$hasFAVTable"
fi

nonExistNode=$(echo $zkInfo | grep "Node does not exist")
if [[ "$nonExistNode" != "" ]]; then
    sh failTest.sh "There are nodes not exist in ZK:$nonExistNode"
fi
echo $zkInfo
COMMENT
