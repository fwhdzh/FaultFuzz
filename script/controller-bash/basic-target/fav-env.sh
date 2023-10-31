OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

export MSG_OPT=false
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,useMsgid=false,jdkMsg=false"

ANNOTATION_FILE_OPT="annotationFile=/data/fengwenhan/code/faultfuzz/info.txt"
USE_INJECT_ANNOTATION_OPT="useInjectAnnotation=true"

RECORD_PATH_OPT="recordPath=/data/fengwenhan/data/faultfuzz_bt/inst_record"
CACHE_DIR_OPT="cacheDir=/data/fengwenhan/data/faultfuzz_bt/inst_cache"
COV_PATH_OPT="covPath=/data/fengwenhan/data/faultfuzz_bt/fuzzcov"
# RECORD_PATH_OPT="recordPath=/data/fengwenhan/data/faultfuzz_bt/inst_test/inst_record"
# CACHE_DIR_OPT="cacheDir=/data/fengwenhan/data/faultfuzz_bt/inst_test/inst_cache"
# COV_PATH_OPT="covPath=/data/fengwenhan/data/faultfuzz_bt/inst_test/fuzzcov"

# COV_INCLUDES_OPTS

IO_ALLOW_OPT="ioAllow=ioAllowlist"
IO_DENY_OPT="ioDeny=ioDenylist"


export FAV_OPTS="-Xbootclasspath/a:${OWN_DIR}/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:${OWN_DIR}/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,jdkFile=true,${RECORD_PATH_OPT},${CACHE_DIR_OPT},${COV_PATH_OPT},dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,controllerSocket=127.0.0.1:12091,covIncludes=edu/iscas/tcse/bt,aflAllow=allowlist,aflDeny=denylist,${IO_ALLOW_OPT},${IO_DENY_OPT},aflPort=12081,execMode=FaultFuzz,${ANNOTATION_FILE_OPT},${USE_INJECT_ANNOTATION_OPT}"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"

