java -Xmx2g -Xms512m -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12093 "hb.properties"

sh restartAllDockers.sh
echo 1

