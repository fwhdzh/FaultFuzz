export MSG_OPT=false
#export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=false,hbaseRpc=true,cacheDir=/home/gaoyu/evaluation/hbase-1.7.1/favcache"
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=false,useMsgid=false,jdkMsg=false"
#export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=false"
#export FAV_OPTS="$PHOS_OPTS"

export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFav=true,forZk=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPhase=true,recordPath=/home/gaoyu/zk363-fav-rst/,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/home/gaoyu/CacheFolder,favHome=/home/gaoyu,currentFault=$FAV_HOME/zk381curFault,controllerSocket=172.30.0.1:12090,strictCheck=false,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/zookeeper,aflAllow=/home/gaoyu/evaluation/zk-3.8.1/allowlist,aflDeny=/home/gaoyu/evaluation/zk-3.8.1/denylist,aflPort=12081"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"

