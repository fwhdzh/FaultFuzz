export MSG_OPT=false
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,hbaseRpc=true"
export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPath=/home/gaoyu/hb244-fav-rst/,dataPaths=/home/gaoyu/evaluation/hbase-2.5.4/tmp:/home/gaoyu/evaluation/hbase-2.5.4/conf,cacheDir=/home/gaoyu/HB244-CacheFolder,hdfsApi=true,zkApi=true,forHbase=true,currentFault=$FAV_HOME/hb244curFault,controllerSocket=124.16.138.61:14900,strictCheck=false,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/hadoop/hbase,aflAllow=/home/gaoyu/evaluation/hbase-2.5.4/allowlist,aflDeny=/home/gaoyu/evaluation/hbase-2.5.4/denylist,aflPort=12181"
export TIME_OPTS="-Dfile.encoding=UTF8 -Duser.timezone=GMT+08"
