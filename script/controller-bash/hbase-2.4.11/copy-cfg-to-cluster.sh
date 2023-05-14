SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
CONFIG_DIR=$SCRIPT_DIR/../../../configuration/hbase-2.4.11


# docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg C1ZK1:/home/gaoyu/evaluation/zk-3.8.1/conf
# docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg C1ZK2:/home/gaoyu/evaluation/zk-3.8.1/conf
# docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg C1ZK3:/home/gaoyu/evaluation/zk-3.8.1/conf
# docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg C1ZK4:/home/gaoyu/evaluation/zk-3.8.1/conf
# docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg C1ZK5:/home/gaoyu/evaluation/zk-3.8.1/conf


source $SCRIPT_DIR/cluster-info.sh

# for name in "${clusterName[@]}"
# do
#     echo "docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1/conf"
#     docker cp $CONFIG_DIR/zk-3.8.1/zoo.cfg $name:/home/gaoyu/evaluation/zk-3.8.1/conf
#     echo "docker cp $CONFIG_DIR/zk-3.8.1/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1/bin"
#     docker cp $CONFIG_DIR/zk-3.8.1/zkEnv.sh $name:/home/gaoyu/evaluation/zk-3.8.1/bin
#     echo "docker cp $CONFIG_DIR/zk-3.8.1/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1/conf"
#     docker cp $CONFIG_DIR/zk-3.8.1/log4j.properties $name:/home/gaoyu/evaluation/zk-3.8.1/conf
# done

for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hb-zk" ]]; then
    continue
  fi
  if [[ "$name" == "C1hb-hdfs" ]]; then
    continue
  fi
  echo "docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hbase-2.4.11/conf'"
  docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hbase-2.4.11/conf'
  echo "docker cp $CONFIG_DIR/conf $name:/home/gaoyu/evaluation/hbase-2.4.11"
  docker cp $CONFIG_DIR/conf $name:/home/gaoyu/evaluation/hbase-2.4.11
done
