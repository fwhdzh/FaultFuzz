# java -Xmx2g -Xms512m -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12090 "/home/fengwenhan/code/faultfuzz/script/controller-bash/zk-3.8.1/zk.properties"
java -Xmx2g -Xms512m -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12090 "/home/fengwenhan/code/faultfuzz/script/controller-bash/zk-3.8.1/zk.properties"

sh restartAllDockers.sh
echo 1

