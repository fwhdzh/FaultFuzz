function waitMaster(){
start=$(date +%s)

while [ 0 -le 5 ]
do

node=$1
info=$2
#docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"
rst=$(docker exec -t C1hb-zk bin/bash -ic "/home/gaoyu/evaluation/hb-zk-3.6.3/bin/zkCli.sh -server localhost:11181 get $node > tmpout.txt && tail -n 5 tmpout.txt")
got=$( echo "$rst" | grep "$info" )
if [[ "$got" == "" ]]; then
#  echo "!!!!!!!!!!!!wait master info $node:$rst"
  echo "$rst"
  break

fi

end=$(date +%s)
#take=$(( end - start ))
let start_=10#${start}
let end_=10#${end}
take=$(( end_ - start_ ))/60

if [[ $take -ge 10 ]]; then
  sh failTest.sh "The master node didn't online in 10 mins!"
  sh jpsCluster.sh
  exit 0
  break

fi

done
}

waitMaster "/hbase/master" "Node does not exist"
waitMaster "/hbase/running" "Node does not exist"
echo "The HMaster has been online!!!"
