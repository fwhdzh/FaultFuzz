OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

java -cp ${OWN_DIR}/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain "${OWN_DIR}/dfs.properties"

sh ${OWN_DIR}/restartAllDockers.sh


echo 1

