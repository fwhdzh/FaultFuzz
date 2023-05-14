SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

DEST_DIR=/data/fengwenhan/data/crashfuzz_hbase_jar_and_start

cp /home/fengwenhan/code/crashfuzz-ctrl/crashfuzz/target/CCrashFuzzer-0.0.1-SNAPSHOT.jar $DEST_DIR
cp $SCRIPT_DIR/crashfuzzer-alone.sh $DEST_DIR
cp $SCRIPT_DIR/crashfuzzer-nohup.sh $DEST_DIR
cp $SCRIPT_DIR/random-alone.sh $DEST_DIR
cp $SCRIPT_DIR/random-nohup.sh $DEST_DIR

