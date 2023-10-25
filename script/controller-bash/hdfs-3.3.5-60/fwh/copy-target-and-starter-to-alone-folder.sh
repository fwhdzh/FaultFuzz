OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

DEST_DIR=/home/fengwenhan/data/faultfuzz_hdfs_jar_and_start

cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar $DEST_DIR
cp $OWN_DIR/faultfuzzer-alone.sh $DEST_DIR
cp $OWN_DIR/faultfuzzer-nohup.sh $DEST_DIR
cp $OWN_DIR/restartAllDockers.sh $DEST_DIR

