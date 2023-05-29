OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $OWN_DIR/random-alone.sh > /home/fengwenhan/data/faultfuzz_hbase_logs/faultfuzz_hbase_nohup.log &