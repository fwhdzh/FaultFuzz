OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

START_TIME=`date +%s`

source ${OWN_DIR}/fav-env.sh
echo $FAV_OPTS

# fwh/fav-jre-inst/bin/java $FAV_OPTS -jar /home/fengwenhan/code/TestPhosphor/target/FaultFuzz-workload-BasicInstTarget-1.0-SNAPSHOT.jar | tee /data/fengwenhan/data/faultfuzz_bt/inst_logs/bt.out

INSTRUMENTED_JAVA=/data/fengwenhan/data/fav-jre-inst

nohup ${INSTRUMENTED_JAVA}/bin/java $FAV_OPTS -cp ${OWN_DIR}/FaultFuzz-workload-BasicInstTarget-1.0-SNAPSHOT.jar edu.iscas.tcse.bt.BTMain | tee /data/fengwenhan/data/faultfuzz_bt/inst_logs/bt.out &

sleep 40



END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME