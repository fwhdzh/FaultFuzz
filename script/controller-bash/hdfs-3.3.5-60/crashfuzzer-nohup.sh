SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $SCRIPT_DIR/faultfuzzer-alone.sh > /home/fengwenhan/data/faultfuzz_hdfs_logs/faultfuzz_hdfs_nohup.log &


