SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

nohup $SCRIPT_DIR/crashfuzzer-alone.sh > /home/fengwenhan/data/crashfuzz_zk_logs/crashfuzz_zk_nohup.log &


