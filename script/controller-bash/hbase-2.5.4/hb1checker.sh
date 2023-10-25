OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

function checkAlive(){
  node=$1
  character=$2
  workerRst=$(docker exec -t $node /bin/bash -ic 'jps')
  result=$(echo $workerRst | grep "${character}")
  if [[ "$result" == "" ]]; then
        echo "jps $node: $workerRst"
        sh ${OWN_DIR}/failTest.sh "${node} ${character} was not started"
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
      172.27.0.2)
        checkAlive "C1HM1" "HMaster"
        ;;
      172.27.0.3)
        checkAlive "C1HM2" "HMaster"
        ;;
      172.27.0.4)
        checkAlive "C1RS1" "HRegionServer"
        ;;
      172.27.0.5)
        checkAlive "C1RS2" "HRegionServer"
        ;;
      172.27.0.6)
        checkAlive "C1RS3" "HRegionServer"
        ;;
    esac
done

deadarr=(${deadNodes//,/ })

for s in ${deadarr[@]}
do
    echo "dead: $s" 
    case $s in
      172.27.0.2)
        ;;
      172.27.0.3)
        ;;
      172.27.0.4)
#        hm1log=$(docker exec -t C1HM1 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs1\" logs")
#        hm2log=$(docker exec -t C1HM2 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs1\" logs")
#        echo "For the crash node $s, the recovery info is $hm1log and $hm2log."
#        if [ "$hm1log" == "" ] && [ "$hm2log" == "" ]; then
#           sh failTest.sh "The failure of node $s seems was not handled by HMaster!"
#        fi
        ;;
      172.27.0.5)
#        hm1log=$(docker exec -t C1HM1 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs2\" logs")
#        hm2log=$(docker exec -t C1HM2 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs2\" logs")
#        echo "For the crash node $s, the recovery info is $hm1log and $hm2log."
#        if [ "$hm1log" == "" ] && [ "$hm2log" == "" ]; then
#           sh failTest.sh "The failure of node $s seems was not handled by HMaster!"
#        fi
        ;; 
      172.27.0.6)
#        hm1log=$(docker exec -t C1HM1 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs3\" logs")
#        hm2log=$(docker exec -t C1HM2 /bin/bash -ic "cd /home/gaoyu/evaluation/hbase-2.5.4/ && grep -r \"Processing expiration of c1rs3\" logs")
#        echo "For the crash node $s, the recovery info is $hm1log and $hm2log."
#        if [ "$hm1log" == "" ] && [ "$hm2log" == "" ]; then
#           sh failTest.sh "The failure of node $s seems was not handled by HMaster!"
#        fi
        ;;
    esac
done

sh ${OWN_DIR}/jpsCluster.sh

sh ${OWN_DIR}/checkException.sh $3

jpsAll=$( sh jpsCluster.sh)
hasRunJar=$(echo $jpsAll| grep "JarBootstrapMain")
if [[ "$hasRunJar" != "" ]]; then
    sh ${OWN_DIR}/failTest.sh "The client was not exit!"

fi

zkInfo=$( sh getZKInfo.sh )
hasFAVTable=$(echo $zkInfo | grep "FAVMyInfo")
if [[ "$hasFAVTable" != "" ]]; then
    sh ${OWN_DIR}/failTest.sh "The FAVMyInfo node is still in ZK:$hasFAVTable"
fi

nonExistNode=$(echo $zkInfo | grep "Node does not exist")
if [[ "$nonExistNode" != "" ]]; then
    sh ${OWN_DIR}/failTest.sh "There are nodes not exist in ZK:$nonExistNode"
fi
echo $zkInfo
