mkdir $1/monitor

mkdir $1/monitor/C1ZK1
docker cp C1ZK1:/zookeeper-3.6.3/logs $1/monitor/C1ZK1
docker cp C1ZK1:/zookeeper-3.6.3/zkData $1/monitor/C1ZK1

mkdir $1/monitor/C1ZK2
docker cp C1ZK2:/zookeeper-3.6.3/logs $1/monitor/C1ZK2
docker cp C1ZK2:/zookeeper-3.6.3/zkData $1/monitor/C1ZK2

mkdir $1/monitor/C1ZK3
docker cp C1ZK3:/zookeeper-3.6.3/logs $1/monitor/C1ZK3
docker cp C1ZK3:/zookeeper-3.6.3/zkData  $1/monitor/C1ZK3

mkdir $1/monitor/C1ZK4
docker cp C1ZK4:/zookeeper-3.6.3/logs $1/monitor/C1ZK4
docker cp C1ZK4:/zookeeper-3.6.3/zkData  $1/monitor/C1ZK4

mkdir $1/monitor/C1ZK5
docker cp C1ZK5:/zookeeper-3.6.3/logs $1/monitor/C1ZK5
docker cp C1ZK5:/zookeeper-3.6.3/zkData  $1/monitor/C1ZK5

mkdir $1/fav-rst

observerRecordPath=/observer/fav-rst
echo $observerRecordPath

docker exec -t C1ZK1 /bin/bash -ic "ls $observerRecordPath"
docker cp C1ZK1:$observerRecordPath $1/fav-rst/
mv $1/fav-rst/fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/fav-rst
docker cp C1ZK2:$observerRecordPath $1/fav-rst/
mv $1/fav-rst/fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/fav-rst
docker cp C1ZK3:$observerRecordPath $1/fav-rst/
mv $1/fav-rst/fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/fav-rst
docker cp C1ZK4:$observerRecordPath $1/fav-rst/
mv $1/fav-rst/fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/fav-rst
docker cp C1ZK5:$observerRecordPath $1/fav-rst/
mv $1/fav-rst/fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/fav-rst

observerCovPath=/observer/fuzzcov

mkdir $1/cov
mkdir $1/cov/C1ZK1
docker cp C1ZK1:$observerCovPath $1/cov/C1ZK1

mkdir $1/cov/C1ZK2
docker cp C1ZK2:$observerCovPath $1/cov/C1ZK2

mkdir $1/cov/C1ZK3
docker cp C1ZK3:$observerCovPath $1/cov/C1ZK3

mkdir $1/cov/C1ZK4
docker cp C1ZK4:$observerCovPath $1/cov/C1ZK4

mkdir $1/cov/C1ZK5
docker cp C1ZK5:$observerCovPath $1/cov/C1ZK5

ls $1


