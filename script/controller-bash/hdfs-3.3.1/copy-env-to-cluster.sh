OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
# echo "copy fav-env to cluster..."
# for name in "${clusterName[@]}"
# do
#   echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1"
#   docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1
# done

for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hd-zk" ]]; then
    echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3"
    docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3
  else
    echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1"
    docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hadoop-3.3.1
  fi
done
