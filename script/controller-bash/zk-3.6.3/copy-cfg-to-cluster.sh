OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# CONFIG_DIR=$OWN_DIR/../../configuration
CONFIG_DIR=$OWN_DIR/configuration


docker cp $CONFIG_DIR/zoo.cfg C1ZK1:/home/gaoyu/evaluation/zk-3.6.3/conf
docker cp $CONFIG_DIR/zoo.cfg C1ZK2:/home/gaoyu/evaluation/zk-3.6.3/conf
docker cp $CONFIG_DIR/zoo.cfg C1ZK3:/home/gaoyu/evaluation/zk-3.6.3/conf
docker cp $CONFIG_DIR/zoo.cfg C1ZK4:/home/gaoyu/evaluation/zk-3.6.3/conf
docker cp $CONFIG_DIR/zoo.cfg C1ZK5:/home/gaoyu/evaluation/zk-3.6.3/conf
