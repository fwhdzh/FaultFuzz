OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

zk363_host=$OWN_DIR/zk363curCrash
zk363_node=/home/gaoyu

docker cp $zk363_host C1ZK1:$zk363_node
docker cp $zk363_host C1ZK2:$zk363_node
docker cp $zk363_host C1ZK3:$zk363_node 
docker cp $zk363_host C1ZK4:$zk363_node
docker cp $zk363_host C1ZK5:$zk363_node
