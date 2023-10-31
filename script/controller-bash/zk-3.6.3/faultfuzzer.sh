OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

java -cp $OWN_DIR/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain "$OWN_DIR/backend-configuration/FaultFuzz-backend-configuration.properties" 

sh $OWN_DIR/backend-configuration/restartAllDockers.sh
echo 1

