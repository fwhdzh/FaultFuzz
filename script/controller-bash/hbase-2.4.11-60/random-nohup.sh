SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $SCRIPT_DIR/random-alone.sh > /home/fengwenhan/data/crashfuzz_hbase_logs/crashfuzz_hbase_nohup.log &