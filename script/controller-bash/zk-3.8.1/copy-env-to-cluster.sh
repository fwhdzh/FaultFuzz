OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


docker cp $OWN_DIR/fav-env.sh C1ZK1:/home/gaoyu/evaluation/zk-3.8.1 
docker cp $OWN_DIR/fav-env.sh C1ZK2:/home/gaoyu/evaluation/zk-3.8.1 
docker cp $OWN_DIR/fav-env.sh C1ZK3:/home/gaoyu/evaluation/zk-3.8.1 
docker cp $OWN_DIR/fav-env.sh C1ZK4:/home/gaoyu/evaluation/zk-3.8.1 
docker cp $OWN_DIR/fav-env.sh C1ZK5:/home/gaoyu/evaluation/zk-3.8.1 
