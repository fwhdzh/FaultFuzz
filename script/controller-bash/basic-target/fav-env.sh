export MSG_OPT=false
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=false,useMsgid=false,jdkMsg=false"

export FAV_OPTS="-Xbootclasspath/a:/home/fengwenhan/code/faultfuzz/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/fengwenhan/code/faultfuzz/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=true,jdkFile=false,recordPhase=true,recordPath=/data/fengwenhan/data/faultfuzz_bt/inst_record,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/data/fengwenhan/data/faultfuzz_bt/inst_cache,currentCrash=/home/fengwenhan/code/faultfuzz/script/controller-bash/bt/btCurCrash,controllerSocket=127.0.0.1:12091,covPath=/data/fengwenhan/data/faultfuzz_bt/fuzzcov,covIncludes=edu/iscas/tcse/bt,aflAllow=/home/fengwenhan/code/faultfuzz/script/controller-bash/basic-target/allowlist,aflDeny=/home/fengwenhan/code/faultfuzz/script/controller-bash/basic-target/denylist,aflPort=12081,execMode=FaultFuzz"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"

