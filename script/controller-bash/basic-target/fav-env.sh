OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

export MSG_OPT=false
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,useMsgid=false,jdkMsg=false"

ANNOTATION_FILE_OPT="annotationFile=/data/fengwenhan/code/faultfuzz/info.txt"

export FAV_OPTS="-Xbootclasspath/a:${OWN_DIR}/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:${OWN_DIR}/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,jdkFile=true,recordPath=/data/fengwenhan/data/faultfuzz_bt/inst_record,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/data/fengwenhan/data/faultfuzz_bt/inst_cache,controllerSocket=127.0.0.1:12091,covPath=/data/fengwenhan/data/faultfuzz_bt/fuzzcov,covIncludes=edu/iscas/tcse/bt,aflAllow=allowlist,aflDeny=denylist,aflPort=12081,execMode=FaultFuzz,${ANNOTATION_FILE_OPT}"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"

