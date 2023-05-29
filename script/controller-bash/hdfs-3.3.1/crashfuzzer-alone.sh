java -Xmx2g -Xms512m -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12092 "/home/fengwenhan/code/faultfuzz/script/controller-bash/hdfs-3.3.1/dfs.properties"

sh restartAllDockers.sh
echo 1

