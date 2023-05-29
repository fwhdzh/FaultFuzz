OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

#docker exec -t C1hb-hdfs /bin/bash -ic '/home/gaoyu/evaluation/hb-hadoop-3.2.2/bin/hdfs namenode -format'
docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && rm -rf tmp && rm -rf nndir && rm -rf dndir && cp -r tmp-init tmp && cp -r dndir-init dndir && cp -r nndir-init nndir && ls'

docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && bin/hdfs --daemon start namenode && jps'
docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && bin/hdfs --daemon start secondarynamenode && jps'
docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && bin/hdfs --daemon start datanode && jps'
#docker exec -t NM3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && bin/hdfs --daemon start datanode && jps'

function waitHDFS(){
start=$(date +%s)

while [ 0 -le 5 ]
do

rst=$(docker exec -t C1hb-hdfs /bin/bash -ic 'cd /home/gaoyu/evaluation/hb-hadoop-3.2.2/ && bin/hdfs dfsadmin -safemode get' | grep "Safe mode is OFF")

if [[ "$rst" != "" ]]; then

  break

fi

end=$(date +%s)
#take=$(( end - start ))
let start_=10#${start}
let end_=10#${end}
take=$(( end_ - start_ ))/60

if [[ $take -ge 10 ]]; then
  sh $OWN_DIR/failTest.sh "The hdfs does not exit safe mode in 10 mins!"
  sh $OWN_DIR/jpsCluster.sh
  exit 0
  break

fi

done
}

waitHDFS
echo "HDFS exists safe mode!"

