OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# CONFIG_DIR=$OWN_DIR/../../../configuration
CONFIG_DIR=$OWN_DIR/configuration

source $OWN_DIR/cluster-info.sh

for name in "${clusterName[@]}"
do
    echo "docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1/conf"
    docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1/conf
    echo "docker cp $CONFIG_DIR/zk-3.8.1/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1/bin"
    docker cp $CONFIG_DIR/zk-3.8.1/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1/bin
    echo "docker cp $CONFIG_DIR/zk-3.8.1/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1/conf"
    docker cp $CONFIG_DIR/zk-3.8.1/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1/conf
done