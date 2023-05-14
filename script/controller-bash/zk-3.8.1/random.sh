java -Xmx2g -Xms512m -cp /home/fengwenhan/code/crashfuzz-ctrl/crashfuzz/target/CCrashFuzzer-0.0.1-SNAPSHOT.jar edu.iscas.CCrashFuzzer.random.RandomFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1/random.properties"
sh restartAllDockers.sh
