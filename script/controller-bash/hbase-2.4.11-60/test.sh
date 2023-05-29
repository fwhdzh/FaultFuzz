OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

java -cp $OWN_DIR/FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar edu.iscas.tcse.HBaseCases.NormalTestNew 172.41.0.8 11181 check nullcrash nullstart $OWN_DIR/failTest.sh

END_TIME=`date +%s`
EXECUTING_TIME=`expr $END_TIME - $START_TIME`
echo $EXECUTING_TIME
#sh collectRst.sh
#sh stopHB.sh
#sh prepareEnv.sh
