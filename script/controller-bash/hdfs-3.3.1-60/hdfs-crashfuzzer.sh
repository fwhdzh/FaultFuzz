java -cp FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain 12090 "dfs.properties"
#sh snapshotState.sh
sh restartAllDockers.sh
#java -cp FaultFuzz-inst-0.0.5-SNAPSHOT.jar edu.iscas.tcse.tcse.favtrigger.crash.controller.docker.CrashTriggerSomeMain 11900 "hb.properties" 20:21:22:23:24:25 10
#java -cp FaultFuzz-inst-0.0.5-SNAPSHOT.jar edu.iscas.tcse.tcse.favtrigger.crash.controller.docker.fortest.InjectOneCrashMain 11900 "hb.properties"


echo 1

