# java -Xmx2g -Xms512m -cp CCrashFuzzer-0.0.1-SNAPSHOT.jar edu.iscas.CCrashFuzzer.CloudFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk/zk.properties"
java -Xmx2g -Xms512m -cp CCrashFuzzer-0.0.1-SNAPSHOT.jar edu.iscas.CCrashFuzzer.CloudFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk/zk.properties"

sh restartAllDockers.sh
echo 1

