java -Xmx2g -Xms512m -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12093 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/hbase-2.4.11/hb.properties"

sh restartAllDockers.sh
echo 1

