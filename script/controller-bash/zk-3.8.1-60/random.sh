java -Xmx2g -Xms512m -cp /home/fengwenhan/code/crashfuzz-ctrl/faultfuzz/target/FaultFuzz-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.random.RandomFuzzMain 12090 "/home/fengwenhan/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1-60/random.properties"
sh restartAllDockers.sh
