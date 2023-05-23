java -Xmx2g -Xms512m -cp FaultFuzz-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.CloudFuzzMain 12092 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/hdfs-3.3.5-60/dfs.properties"

sh restartAllDockers.sh
echo 1

