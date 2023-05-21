#sh clearRst.sh
#sh clearDockerRst.sh
#sh clearLogs.sh
#sh clearHB.sh
START_TIME=`date +%s`
docker exec -t C1Master1 /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.1/bin/hdfs zkfc -formatZK -force && jps'

#add jps command to avoid SIGHUP
echo "Start journal node on C1Slave1:"
docker exec -t C1Slave1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start journalnode && jps'
echo "Start journal node on C1Slave2:"
docker exec -t C1Slave2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start journalnode && jps'
echo "Start journal node on C1Slave3:"
docker exec -t C1Slave3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start journalnode && jps'

echo "Start namenode on C1NN:"
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start namenode && jps'
#docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs namenode -bootstrapStandby -force'
echo "Start namenode on C1Master1:"
docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start namenode && jps'

echo "Start zkfc on C1NN:"
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start zkfc && jps'
echo "Start zkfc on C1Master1:"
docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start zkfc && jps'

echo "Start datanode on C1Slave1:"
docker exec -t C1Slave1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start datanode && jps'
echo "Start datanode on C1Slave2:"
docker exec -t C1Slave2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start datanode && jps'
echo "Start datanode on C1Slave3:"
docker exec -t C1Slave3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start datanode && jps'

echo "waiting for active namenode ..."
sh activeNN.sh
#sh leaveSafe.sh

export PHOS_OPTS="-Xbootclasspath/a:Phosphor-0.0.5-SNAPSHOT.jar -javaagent:Phosphor-0.0.5-SNAPSHOT.jar=useFav=false,hdfsRpc=true"

docker cp dfs1-cli.sh C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1
docker cp ubuntuFailTest.sh C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1
docker exec -t C1Slave4 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && sh dfs1-cli.sh'

java -cp dfscases.jar edu.iscas.HDFSCasesV3.NormalTest check start.sh stop.sh /data1/gaoyu/crashfuzzer/hdfs-3.3.1-c1-new/failTest.sh
#fav-jre-inst/bin/java $PHOS_OPTS -cp dfscases.jar edu.iscas.HDFSCasesV3.NormalTest check start.sh stop.sh /data1/gaoyu/crashfuzzer/hdfs-3.3.1-c1-new/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#mkdir init-state
#sh monitor.sh init-state
#sh collectRst.sh
#sh prepareEnv.sh
