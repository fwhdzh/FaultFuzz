OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# CONFIG_DIR=$OWN_DIR/../../../configuration/hdfs
CONFIG_DIR=$OWN_DIR/configuration

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
echo "copy hdfs cfg to cluster..."
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hd-zk" ]]; then
    echo "docker cp $CONFIG_DIR/zkEnv.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3/bin"
    docker cp $CONFIG_DIR/zkEnv.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3/bin
  else
    echo "docker cp $CONFIG_DIR/hadoop-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1/etc/hadoop"
    docker cp $CONFIG_DIR/hadoop-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1/etc/hadoop
  fi
  
done

