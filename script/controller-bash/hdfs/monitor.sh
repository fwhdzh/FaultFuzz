#mv console.txt $1
mkdir $1/monitor

#docker exec -t C1Slave4 /bin/bash -ic "cd /home/gaoyu/evaluation/hadoop-3.3.1 && rm -rf mydata"
#docker exec -t C1Slave4 /bin/bash -ic "cd /home/gaoyu/evaluation/hadoop-3.3.1 && mkdir mydata && bin/hdfs dfs -copyToLocal / mydata"
docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/mydata $1/

mkdir $1/monitor/C1NN
docker cp C1NN:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/monitor/C1NN
docker cp C1NN:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/monitor/C1NN
docker cp C1NN:/home/gaoyu/evaluation/hadoop-3.3.1/nndir $1/monitor/C1NN

#mkdir $1/C1RM
#docker cp C1RM:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1RM
#docker cp C1RM:/home/gaoyu/evaluation/hadoop-3.3.1/rmstore $1/C1RM
#docker cp C1RM:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1RM

mkdir $1/monitor/C1Master1
docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/monitor/C1Master1
docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/monitor/C1Master1
docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/nndir $1/monitor/C1Master1
#docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/rmstore $1/C1Master1
#docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Master1

#mkdir $1/C1Master2
#docker cp C1Master2:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Master2
#docker cp C1Master2:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/C1Master2
#docker cp C1Master2:/home/gaoyu/evaluation/hadoop-3.3.1/nndir $1/C1Master2
#docker cp C1Master2:/home/gaoyu/evaluation/hadoop-3.3.1/rmstore $1/C1Master2
#docker cp C1Master2:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Master2

mkdir $1/monitor/C1Slave1
docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/monitor/C1Slave1
#docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/nmdir $1/C1Slave1
#docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/nmstore $1/C1Slave1
#docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Slave1
docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/monitor/C1Slave1
docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/dndir $1/monitor/C1Slave1
docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/journal-local $1/monitor/C1Slave1

mkdir $1/monitor/C1Slave2
docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/monitor/C1Slave2
#docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/nmdir $1/C1Slave2
#docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/nmstore $1/C1Slave2
#docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Slave2
docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/monitor/C1Slave2
docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/dndir $1/monitor/C1Slave2
docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/journal-local $1/monitor/C1Slave2

mkdir $1/monitor/C1Slave3
docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/monitor/C1Slave3
#docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/nmdir $1/C1Slave3
#docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/nmstore $1/C1Slave3
#docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Slave3
docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/monitor/C1Slave3
docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/dndir $1/monitor/C1Slave3
docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/journal-local $1/monitor/C1Slave3

#mkdir $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/nmdir $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/nmstore $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/am-staging $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/tmp $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/dndir $1/C1Slave4
#docker cp C1Slave4:/home/gaoyu/evaluation/hadoop-3.3.1/journal-local $1/C1Slave4

#mkdir $1/C1HS
#docker cp C1HS:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1HS

#mkdir $1/C1WPS
#docker cp C1WPS:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1WPS

mkdir $1/fav-rst
docker cp C1NN:/home/gaoyu/dfs331-fav-rst $1/fav-rst/
mv $1/fav-rst/dfs331-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/dfs331-fav-rst

docker cp C1Master1:/home/gaoyu/dfs331-fav-rst $1/fav-rst/
mv $1/fav-rst/dfs331-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/dfs331-fav-rst

docker cp C1Slave1:/home/gaoyu/dfs331-fav-rst $1/fav-rst/
mv $1/fav-rst/dfs331-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/dfs331-fav-rst

docker cp C1Slave2:/home/gaoyu/dfs331-fav-rst $1/fav-rst/
mv $1/fav-rst/dfs331-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/dfs331-fav-rst

docker cp C1Slave3:/home/gaoyu/dfs331-fav-rst $1/fav-rst/
mv $1/fav-rst/dfs331-fav-rst/* $1/fav-rst/
rm -r $1/fav-rst/dfs331-fav-rst

mkdir $1/cov
mkdir $1/cov/C1NN
docker cp C1NN:/home/gaoyu/fuzzcov $1/cov/C1NN
#docker cp C1NN:/home/gaoyu/fuzzcov-recs $1/cov/C1NN

mkdir $1/cov/C1Master1
docker cp C1Master1:/home/gaoyu/fuzzcov $1/cov/C1Master1
#docker cp C1Master1:/home/gaoyu/fuzzcov-recs $1/cov/C1Master1

mkdir $1/cov/C1Slave1
docker cp C1Slave1:/home/gaoyu/fuzzcov $1/cov/C1Slave1
#docker cp C1Slave1:/home/gaoyu/fuzzcov-recs $1/cov/C1Slave1

mkdir $1/cov/C1Slave2
docker cp C1Slave2:/home/gaoyu/fuzzcov $1/cov/C1Slave2

mkdir $1/cov/C1Slave3
docker cp C1Slave3:/home/gaoyu/fuzzcov $1/cov/C1Slave3
