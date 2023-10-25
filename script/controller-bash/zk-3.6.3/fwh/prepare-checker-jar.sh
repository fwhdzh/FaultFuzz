OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

CHECKER_PATH=/data/fengwenhan/code/zkcases/ZKCases/target/ZKCases-0.0.1-SNAPSHOT.jar

echo "cp $CHECKER_PATH $OWN_DIR"
cp $CHECKER_PATH $OWN_DIR