export MSG_OPT=false
#export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,hbaseRpc=true,cacheDir=/home/gaoyu/evaluation/hbase-1.7.1/favcache"
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,useMsgid=false,jdkMsg=false"
#export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false"
#export FAV_OPTS="$PHOS_OPTS"

export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,forZk=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPath=/home/gaoyu/zk363-fav-rst/,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/home/gaoyu/CacheFolder,currentFault=$FAV_HOME/zk381curFault,controllerSocket=172.30.0.1:12090,strictCheck=false,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/zookeeper,aflAllow=/home/gaoyu/evaluation/zk-3.8.1/allowlist,aflDeny=/home/gaoyu/evaluation/zk-3.8.1/denylist,aflPort=12081,determineState=2"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"

