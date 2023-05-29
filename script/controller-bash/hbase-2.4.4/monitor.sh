mv console.txt $1
mkdir $1/monitor

mkdir $1/monitor/C1HM1
docker cp C1HM1:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/monitor/C1HM1
#docker cp C1HM1:/home/gaoyu/evaluation/hbase-2.4.8/nmlogs $1/C1HM1
#docker cp C1HM1:/home/gaoyu/evaluation/hbase-2.4.8/nmdir $1/C1HM1
docker cp C1HM1:/home/gaoyu/evaluation/hbase-2.4.8/tmp $1/monitor/C1HM1

mkdir $1/monitor/C1HM2
docker cp C1HM2:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/monitor/C1HM2
#docker cp C1HM2:/home/gaoyu/evaluation/hbase-2.4.8/nmlogs $1/C1HM2
#docker cp C1HM2:/home/gaoyu/evaluation/hbase-2.4.8/nmdir $1/C1HM2
docker cp C1HM2:/home/gaoyu/evaluation/hbase-2.4.8/tmp $1/monitor/C1HM2

mkdir $1/monitor/C1RS1
docker cp C1RS1:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/monitor/C1RS1
#docker cp C1RS1:/home/gaoyu/evaluation/hbase-2.4.8/nmlogs $1/C1RS1
#docker cp C1RS1:/home/gaoyu/evaluation/hbase-2.4.8/nmdir $1/C1RS1
#docker cp C1RS1:/home/gaoyu/evaluation/hbase-2.4.8/dndir $1/C1RS1
docker cp C1RS1:/home/gaoyu/evaluation/hbase-2.4.8/tmp $1/monitor/C1RS1

mkdir $1/monitor/C1RS2
docker cp C1RS2:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/monitor/C1RS2
#docker cp C1RS2:/home/gaoyu/evaluation/hbase-2.4.8/nmlogs $1/C1RS2
#docker cp C1RS2:/home/gaoyu/evaluation/hbase-2.4.8/nmdir $1/C1RS2
#docker cp C1RS2:/home/gaoyu/evaluation/hbase-2.4.8/dndir $1/C1RS2
docker cp C1RS2:/home/gaoyu/evaluation/hbase-2.4.8/tmp $1/monitor/C1RS2

mkdir $1/monitor/C1RS3
docker cp C1RS3:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/monitor/C1RS3
#docker cp C1RS3:/home/gaoyu/evaluation/hbase-2.4.8/nmlogs $1/C1RS3
#docker cp C1RS3:/home/gaoyu/evaluation/hbase-2.4.8/nmdir $1/C1RS3
#docker cp C1RS3:/home/gaoyu/evaluation/hbase-2.4.8/dndir $1/C1RS3
docker cp C1RS3:/home/gaoyu/evaluation/hbase-2.4.8/tmp  $1/monitor/C1RS3

docker exec -t C1hb-hdfs /bin/bash -ic "cd /home/gaoyu/evaluation/hb-hadoop-3.2.2 && rm -rf hbase"
docker exec -t C1hb-hdfs /bin/bash -ic "cd /home/gaoyu/evaluation/hb-hadoop-3.2.2 && bin/hadoop fs -copyToLocal /hbase ."

mkdir $1/C1hb-hdfs
docker cp C1hb-hdfs:/home/gaoyu/evaluation/hb-hadoop-3.2.2/hbase $1/C1hb-hdfs
docker exec -t C1hb-hdfs /bin/bash -ic "cd /home/gaoyu/evaluation/hb-hadoop-3.2.2 && rm -rf hbase"
#docker cp C1hb-hdfs:/home/gaoyu/evaluation/hb-hadoop-3.2.2/logs $1/C1hb-hdfs
docker cp C1hb-hdfs:/home/gaoyu/evaluation/hb-hadoop-3.2.2/tmp $1/C1hb-hdfs
docker cp C1hb-hdfs:/home/gaoyu/evaluation/hb-hadoop-3.2.2/dndir $1/C1hb-hdfs
docker cp C1hb-hdfs:/home/gaoyu/evaluation/hb-hadoop-3.2.2/nndir $1/C1hb-hdfs

mkdir $1/fav-rst
docker cp C1HM1:/home/gaoyu/hb244-fav-rst $1/fav-rst/
mv $1/fav-rst/hb244-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/hb244-fav-rst

docker cp C1HM2:/home/gaoyu/hb244-fav-rst $1/fav-rst/
mv $1/fav-rst/hb244-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/hb244-fav-rst

docker cp C1RS1:/home/gaoyu/hb244-fav-rst $1/fav-rst/
mv $1/fav-rst/hb244-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/hb244-fav-rst

docker cp C1RS2:/home/gaoyu/hb244-fav-rst $1/fav-rst/
mv $1/fav-rst/hb244-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/hb244-fav-rst

docker cp C1RS3:/home/gaoyu/hb244-fav-rst $1/fav-rst/
mv $1/fav-rst/hb244-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/hb244-fav-rst

mkdir $1/cov
mkdir $1/cov/C1HM1
docker cp C1HM1:/home/gaoyu/fuzzcov $1/cov/C1HM1

mkdir $1/cov/C1HM2
docker cp C1HM2:/home/gaoyu/fuzzcov $1/cov/C1HM2

mkdir $1/cov/C1RS1
docker cp C1RS1:/home/gaoyu/fuzzcov $1/cov/C1RS1

mkdir $1/cov/C1RS2
docker cp C1RS2:/home/gaoyu/fuzzcov $1/cov/C1RS2

mkdir $1/cov/C1RS3
docker cp C1RS3:/home/gaoyu/fuzzcov $1/cov/C1RS3
