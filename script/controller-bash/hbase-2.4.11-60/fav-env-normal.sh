export MSG_OPT=false
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,hbaseRpc=true"
export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPath=/home/gaoyu/hb244-fav-rst/,dataPaths=/home/gaoyu/evaluation/hbase-2.4.11/tmp:/home/gaoyu/evaluation/hbase-2.4.11/conf,cacheDir=/home/gaoyu/HB244-CacheFolder,hdfsApi=true,zkApi=true,forHbase=true,currentFault=$FAV_HOME/hb244curFault,controllerSocket=172.41.0.1:12093,strictCheck=false,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/hadoop/hbase,aflAllow=/home/gaoyu/evaluation/hbase-2.4.11/allowlist,aflDeny=/home/gaoyu/evaluation/hbase-2.4.11/denylist,aflPort=12181,replayMode=false,replayNow=false,determineState=0"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"
