SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

#sh clearRst.sh
#sh clearDockerRst.sh
#sh clearLogs.sh
#sh clearHB.sh
START_TIME=`date +%s`

#sh ../startNewHB.sh
#start hbase cluster: 2 hm 2 rs
docker exec -t C1HM1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8/ && bin/hbase-daemon.sh start master && jps'
docker exec -t C1HM2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8/ && bin/hbase-daemon.sh start master && jps'
docker exec -t C1RS1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8/ && bin/hbase-daemon.sh start regionserver && jps'
docker exec -t C1RS2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8/ && bin/hbase-daemon.sh start regionserver && jps'
docker exec -t C1RS3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8/ && bin/hbase-daemon.sh start regionserver && jps'

sh $SCRIPT_DIR/masterOnline.sh
export PHOS_OPTS="-Xbootclasspath/a:Phosphor-0.0.5-SNAPSHOT.jar -javaagent:Phosphor-0.0.5-SNAPSHOT.jar=useFav=false,hbaseRpc=true"

#fav-jre-inst/bin/java $PHOS_OPTS -cp HBaseCases-0.0.1-SNAPSHOT.jar com.iscas.HBaseCases.NormalTestNew 172.25.0.8 11181 check nullcrash nullstart /data/gaoyu/crashfuzzer/hbase-2.4.8-c1/failTest.sh

java -cp $SCRIPT_DIR/HBaseCases-0.0.1-SNAPSHOT.jar com.iscas.HBaseCases.NormalTestNew 172.27.0.8 11181 check nullcrash nullstart $SCRIPT_DIR/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#sh collectRst.sh
#sh stopHB.sh
#sh prepareEnv.sh
