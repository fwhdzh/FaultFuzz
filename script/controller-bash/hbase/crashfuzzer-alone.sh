java -Xmx2g -Xms512m -cp FaultFuzz-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.CloudFuzzMain 12093 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/hbase/hb.properties"

sh restartAllDockers.sh
echo 1

