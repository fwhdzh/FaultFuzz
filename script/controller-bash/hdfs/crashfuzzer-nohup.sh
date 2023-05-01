SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $SCRIPT_DIR/crashfuzzer-alone.sh > /data/fengwenhan/data/crashfuzz_hdfs_logs/crashfuzz_hdfs_nohup.log &


