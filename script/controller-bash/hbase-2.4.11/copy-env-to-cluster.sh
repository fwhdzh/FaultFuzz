OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hb-zk" ]]; then
    # echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3"
    # docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3
    continue
  fi
  if [[ "$name" == "C1hb-hdfs" ]]; then
    # echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3"
    # docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3
    continue
  fi
  echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hbase-2.4.11"
  docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hbase-2.4.11
  echo "docker cp $OWN_DIR/denylist $name:/home/gaoyu/evaluation/hbase-2.4.11"
  docker cp $OWN_DIR/denylist $name:/home/gaoyu/evaluation/hbase-2.4.11
done
