SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

DEST_DIR=/data/fengwenhan/data/faultfuzz_hdfs_jar_and_start

cp /home/fengwenhan/code/faultfuzz/ctrl/target/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar $DEST_DIR
cp $SCRIPT_DIR/faultfuzzer-alone.sh $DEST_DIR
cp $SCRIPT_DIR/faultfuzzer-nohup.sh $DEST_DIR
cp $SCRIPT_DIR/restartAllDockers.sh $DEST_DIR

