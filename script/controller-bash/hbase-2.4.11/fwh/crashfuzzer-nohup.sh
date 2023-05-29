OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $OWN_DIR/faultfuzzer-alone.sh > /data/fengwenhan/data/faultfuzz_hbase_logs/faultfuzz_hbase_nohup.log &


