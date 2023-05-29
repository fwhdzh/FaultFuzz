OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


# docker cp $OWN_DIR/fav-env.sh C1ZK1:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK2:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK3:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK4:/home/gaoyu/evaluation/zk-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK5:/home/gaoyu/evaluation/zk-3.6.3 

mkdir $1
mkdir $1/C1ZK1
mkdir $1/C1ZK2
mkdir $1/C1ZK3
mkdir $1/C1ZK4
mkdir $1/C1ZK5
docker cp C1ZK1:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK1
docker cp C1ZK2:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK2
docker cp C1ZK3:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK3
docker cp C1ZK4:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK4
docker cp C1ZK5:/home/gaoyu/evaluation/zk-3.6.3/logs $1/C1ZK5

# docker exec -t C1ZK1 /bin/bash -ic "cd /home/gaoyu/evaluation/zk-3.6.3/logs &&  du -h --max-depth=1"

