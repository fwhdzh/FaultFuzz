SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

DEST_DIR=/home/fengwenhan/data/faultfuzz_zk_jar_and_start

cp /home/fengwenhan/code/crashfuzz-ctrl/faultfuzz/target/FaultFuzz-0.0.1-SNAPSHOT.jar $DEST_DIR
cp $SCRIPT_DIR/faultfuzzer-alone.sh $DEST_DIR
cp $SCRIPT_DIR/faultfuzzer-nohup.sh $DEST_DIR
cp $SCRIPT_DIR/random-alone.sh $DEST_DIR
cp $SCRIPT_DIR/random-nohup.sh $DEST_DIR

