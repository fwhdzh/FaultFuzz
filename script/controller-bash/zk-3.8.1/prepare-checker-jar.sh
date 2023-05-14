SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

CHECKER_PATH=/data/fengwenhan/code/zkcases/ZKCases/target/zkcases-0.jar

echo "cp $CHECKER_PATH $SCRIPT_DIR"
cp $CHECKER_PATH $SCRIPT_DIR