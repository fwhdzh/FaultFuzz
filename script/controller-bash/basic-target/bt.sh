START_TIME=`date +%s`

source /home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/basic-target/fav-env.sh
echo $FAV_OPTS

# /home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/basic-target/fwh/fav-jre-inst/bin/java $FAV_OPTS -jar /home/fengwenhan/code/TestPhosphor/target/BasicInstTarget-1.0-SNAPSHOT.jar | tee /data/fengwenhan/data/faultfuzz_bt/inst_logs/bt.out

nohup /home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/basic-target/fwh/fav-jre-inst/bin/java $FAV_OPTS -cp /home/fengwenhan/code/TestPhosphor/target/BasicInstTarget-1.0-SNAPSHOT.jar  edu.iscas.tcse.bt.BTMain | tee /data/fengwenhan/data/faultfuzz_bt/inst_logs/bt.out &

sleep 40



END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME