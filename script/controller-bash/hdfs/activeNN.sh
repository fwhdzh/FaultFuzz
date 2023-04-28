function waitNN(){
start=$(date +%s)

while [ 0 -le 5 ]
do

info=$1
#rst=$(docker exec -it C1NN bin/bash -ic "cd /home/gaoyu/evaluation/hadoop-3.3.1 && bin/hdfs haadmin -getServiceState nn2")
rst=$(docker exec -t C1hd-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hd-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hadoop-ha/mycluster/ActiveBreadCrumb')
#got=$( echo "$rst" | grep "$info" )
got=$( echo "$rst" | grep "C1NN" )
if [[ "$got" != "" ]]; then
  echo "active nn is C1NN"
  break
fi

got=$( echo "$rst" | grep "C1Master1" )
if [[ "$got" != "" ]]; then
  echo "active nn is C1Master1"
  break
fi

got=$( echo "$rst" | grep "C1Master2" )
if [[ "$got" != "" ]]; then
  echo "active nn is C1Master2"
  break
fi

#rst=$(docker exec -it C1NN bin/bash -ic "cd /home/gaoyu/evaluation/hadoop-3.3.1 && bin/hdfs haadmin -getServiceState nn1")
#got=$( echo "$rst" | grep "$info" )
#if [[ "$got" == "" ]]; then
#  break
#fi

end=$(date +%s)
#take=$(( end - start ))
let start_=10#${start}
let end_=10#${end}
take=$(( end_ - start_ ))/60

if [[ $take -ge 10 ]]; then
  sh failTest.sh "The active namenode didn't online in 10 mins!"
  sh jpsCluster.sh
  exit 0
  break

fi

done
}

#waitNN "active"
waitNN
echo "The active namenode has been online!!!"
