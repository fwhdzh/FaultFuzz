java -Xmx2g -Xms512m -cp /home/fengwenhan/code/faultfuzz/ctrl/target/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.random.RandomFuzzMain 12090 "/home/fengwenhan/code/faultfuzz/script/controller-bash/zk-3.8.1-60/random.properties"
sh restartAllDockers.sh
