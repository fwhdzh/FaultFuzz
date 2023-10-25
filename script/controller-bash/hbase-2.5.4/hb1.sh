OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

#sh clearRst.sh
#sh clearDockerRst.sh
#sh clearLogs.sh
#sh clearHB.sh
START_TIME=`date +%s`

#sh ../startNewHB.sh
#start hbase cluster: 2 hm 2 rs
docker exec -t C1HM1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.5.4/ && bin/hbase-daemon.sh start master && jps'
docker exec -t C1HM2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.5.4/ && bin/hbase-daemon.sh start master && jps'
docker exec -t C1RS1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.5.4/ && bin/hbase-daemon.sh start regionserver && jps'
docker exec -t C1RS2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.5.4/ && bin/hbase-daemon.sh start regionserver && jps'
docker exec -t C1RS3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.5.4/ && bin/hbase-daemon.sh start regionserver && jps'

sh $OWN_DIR/masterOnline.sh
export PHOS_OPTS="-Xbootclasspath/a:FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,hbaseRpc=true"

#fav-jre-inst/bin/java $PHOS_OPTS -cp FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HBaseCases.NormalTestNew 172.25.0.8 11181 check nullcrash nullstart /data/gaoyu/faultfuzzer/hbase-2.5.4-c1/failTest.sh

java -cp $OWN_DIR/FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HBaseCases.NormalTestNew 172.27.0.8 11181 check nullcrash nullstart $OWN_DIR/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#sh collectRst.sh
#sh stopHB.sh
#sh prepareEnv.sh
