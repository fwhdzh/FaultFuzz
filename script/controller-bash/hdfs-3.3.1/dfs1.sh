OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

#sh clearRst.sh
#sh clearDockerRst.sh
#sh clearLogs.sh
#sh clearHB.sh
START_TIME=`date +%s`
echo "Start zkfc on C1Master1"
docker exec -t C1Master1 /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.1/bin/hdfs zkfc -formatZK -force && jps'
# docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && . fav-env.sh && HADOOP_OPTS="" && /home/gaoyu/evaluation/hadoop-3.3.1/bin/hdfs zkfc -formatZK -force && jps'

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
sh $OWN_DIR/activeNN.sh
#sh leaveSafe.sh

export PHOS_OPTS="-Xbootclasspath/a:FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,hdfsRpc=true"

docker cp $OWN_DIR/dfs1-cli.sh C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1
docker cp $OWN_DIR/ubuntuFailTest.sh C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1
docker exec -t C1Slave4 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && sh dfs1-cli.sh'

echo "java -cp $OWN_DIR/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HDFSCasesV3.NormalTest check start.sh stop.sh $OWN_DIR/failTest.sh"
java -cp $OWN_DIR/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HDFSCasesV3.NormalTest check start.sh stop.sh $OWN_DIR/failTest.sh
# java -cp $OWN_DIR/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HDFSCasesV3.NormalTest check start.sh stop.sh /data1/gaoyu/faultfuzzer/hdfs-3.3.1-c1-new/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#mkdir init-state
#sh monitor.sh init-state
#sh collectRst.sh
#sh prepareEnv.sh
