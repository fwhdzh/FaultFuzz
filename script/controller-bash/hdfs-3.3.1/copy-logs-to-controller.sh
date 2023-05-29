OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


# docker cp $OWN_DIR/fav-env.sh C1ZK1:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK2:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK3:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK4:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK5:/home/gaoyu/evaluation/zk-3.6.3 

mkdir $1
mkdir $1/C1NN
mkdir $1/C1Master1
mkdir $1/C1Slave1
mkdir $1/C1Slave2
mkdir $1/C1Slave3
mkdir $1/C1hd-zk

docker cp C1NN:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1NN
docker cp C1Master1:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Master1
docker cp C1Slave1:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Slave1
docker cp C1Slave2:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Slave2
docker cp C1Slave3:/home/gaoyu/evaluation/hadoop-3.3.1/logs $1/C1Slave3
docker cp C1hd-zk:/home/gaoyu/evaluation/hd-zk-3.6.3/logs $1/C1hd-zk

# docker cp C1ZK1:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK1
# docker cp C1ZK2:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK2
# docker cp C1ZK3:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK3
# docker cp C1ZK4:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK4
# docker cp C1ZK5:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK5