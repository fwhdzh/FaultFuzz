OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# CONFIG_DIR=$OWN_DIR/../../../configuration
CONFIG_DIR=$OWN_DIR/configuration

source $OWN_DIR/cluster-info.sh

for name in "${clusterName[@]}"
do
    echo "docker cp $CONFIG_DIR/zk-3.8.1-60/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1-60/conf"
    docker cp $CONFIG_DIR/zk-3.8.1-60/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1-60/conf
    echo "docker cp $CONFIG_DIR/zk-3.8.1-60/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1-60/bin"
    docker cp $CONFIG_DIR/zk-3.8.1-60/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1-60/bin
    echo "docker cp $CONFIG_DIR/zk-3.8.1-60/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1-60/conf"
    docker cp $CONFIG_DIR/zk-3.8.1-60/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1-60/conf
done