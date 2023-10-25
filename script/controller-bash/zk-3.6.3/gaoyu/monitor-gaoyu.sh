mkdir $1/monitor

mkdir $1/monitor/C1ZK1
docker cp C1ZK1:/home/gaoyu/evaluation/zk-3.6.3/logs $1/monitor/C1ZK1
docker cp C1ZK1:/home/gaoyu/evaluation/zk-3.6.3/zkData $1/monitor/C1ZK1

mkdir $1/monitor/C1ZK2
docker cp C1ZK2:/home/gaoyu/evaluation/zk-3.6.3/logs $1/monitor/C1ZK2
docker cp C1ZK2:/home/gaoyu/evaluation/zk-3.6.3/zkData $1/monitor/C1ZK2

mkdir $1/monitor/C1ZK3
docker cp C1ZK3:/home/gaoyu/evaluation/zk-3.6.3/logs $1/monitor/C1ZK3
docker cp C1ZK3:/home/gaoyu/evaluation/zk-3.6.3/zkData  $1/monitor/C1ZK3

mkdir $1/monitor/C1ZK4
docker cp C1ZK4:/home/gaoyu/evaluation/zk-3.6.3/logs $1/monitor/C1ZK4
docker cp C1ZK4:/home/gaoyu/evaluation/zk-3.6.3/zkData  $1/monitor/C1ZK4

mkdir $1/monitor/C1ZK5
docker cp C1ZK5:/home/gaoyu/evaluation/zk-3.6.3/logs $1/monitor/C1ZK5
docker cp C1ZK5:/home/gaoyu/evaluation/zk-3.6.3/zkData  $1/monitor/C1ZK5

mkdir $1/fav-rst
docker cp C1ZK1:/home/gaoyu/zk363-fav-rst $1/fav-rst/
mv $1/fav-rst/zk363-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/zk363-fav-rst
docker cp C1ZK2:/home/gaoyu/zk363-fav-rst $1/fav-rst/
mv $1/fav-rst/zk363-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/zk363-fav-rst
docker cp C1ZK3:/home/gaoyu/zk363-fav-rst $1/fav-rst/
mv $1/fav-rst/zk363-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/zk363-fav-rst
docker cp C1ZK4:/home/gaoyu/zk363-fav-rst $1/fav-rst/
mv $1/fav-rst/zk363-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/zk363-fav-rst
docker cp C1ZK5:/home/gaoyu/zk363-fav-rst $1/fav-rst/
mv $1/fav-rst/zk363-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/zk363-fav-rst

mkdir $1/cov
mkdir $1/cov/C1ZK1
docker cp C1ZK1:/home/gaoyu/fuzzcov $1/cov/C1ZK1
#docker cp C1ZK1:/home/gaoyu/fuzzcov-recs $1/cov/C1ZK1

mkdir $1/cov/C1ZK2
docker cp C1ZK2:/home/gaoyu/fuzzcov $1/cov/C1ZK2
#docker cp C1ZK2:/home/gaoyu/fuzzcov-recs $1/cov/C1ZK2

mkdir $1/cov/C1ZK3
docker cp C1ZK3:/home/gaoyu/fuzzcov $1/cov/C1ZK3
#docker cp C1ZK3:/home/gaoyu/fuzzcov-recs $1/cov/C1ZK3

mkdir $1/cov/C1ZK4
docker cp C1ZK4:/home/gaoyu/fuzzcov $1/cov/C1ZK4

mkdir $1/cov/C1ZK5
docker cp C1ZK5:/home/gaoyu/fuzzcov $1/cov/C1ZK5

<<'COMMENT'
mkdir $1/jacoco

mkdir $1/jacoco/C1ZK1
docker cp C1ZK1:/home/gaoyu/jacoco.exec $1/jacoco/C1ZK1

mkdir $1/jacoco/C1ZK2
docker cp C1ZK1:/home/gaoyu/jacoco.exec $1/jacoco/C1ZK2

mkdir $1/jacoco/C1ZK3
docker cp C1ZK1:/home/gaoyu/jacoco.exec $1/jacoco/C1ZK3

mkdir $1/jacoco/C1ZK4
docker cp C1ZK1:/home/gaoyu/jacoco.exec $1/jacoco/C1ZK4

mkdir $1/jacoco/C1ZK5
docker cp C1ZK1:/home/gaoyu/jacoco.exec $1/jacoco/C1ZK5

java -jar jacoco-0.8.7/lib/jacococli.jar merge $1/jacoco/C1ZK1/jacoco.exec $1/jacoco/C1ZK2/jacoco.exec $1/jacoco/C1ZK3/jacoco.exec $1/jacoco/C1ZK4/jacoco.exec $1/jacoco/C1ZK5/jacoco.exec --destfile $1/cov/merged.exec

java -jar jacoco-0.8.7/lib/jacococli.jar report $1/cov/merged.exec --classfiles $FAV_HOME/evaluation/zk-3.6.3/lib/zookeeper-3.6.3.jar --xml $1/cov/cov.xml

java -jar jacoco-0.8.7/lib/jacococli.jar report $1/cov/merged.exec --classfiles $FAV_HOME/evaluation/zk-3.6.3/lib/zookeeper-3.6.3.jar --html $1/cov/cov-page
COMMENT
