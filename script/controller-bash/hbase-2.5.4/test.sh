SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

java -cp $SCRIPT_DIR/HBaseCases-0.0.1-SNAPSHOT.jar com.iscas.HBaseCases.NormalTestNew 172.27.0.8 11181 check nullcrash nullstart $SCRIPT_DIR/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#sh collectRst.sh
#sh stopHB.sh
#sh prepareEnv.sh
