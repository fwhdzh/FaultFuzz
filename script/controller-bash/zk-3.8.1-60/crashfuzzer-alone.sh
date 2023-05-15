# java -Xmx2g -Xms512m -cp CCrashFuzzer-0.0.1-SNAPSHOT.jar edu.iscas.CCrashFuzzer.CloudFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1-60/zk.properties"
java -Xmx2g -Xms512m -cp CCrashFuzzer-0.0.1-SNAPSHOT.jar edu.iscas.CCrashFuzzer.CloudFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1-60/zk.properties"

sh restartAllDockers.sh
echo 1

