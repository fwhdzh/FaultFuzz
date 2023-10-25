OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $OWN_DIR/random-alone.sh > /data/fengwenhan/data/faultfuzz_zk_logs/faultfuzz_zk_nohup.log &