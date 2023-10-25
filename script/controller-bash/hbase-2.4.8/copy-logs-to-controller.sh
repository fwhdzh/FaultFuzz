OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "mkdir $1"
mkdir $1

# 循环遍历数组，对每个元素执行命令
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hb-zk" ]]; then
    # echo "docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3"
    # docker cp $OWN_DIR/fav-env.sh $name:/home/gaoyu/evaluation/hd-zk-3.6.3
    echo "$1/$name"
    mkdir $1/$name
    echo "docker cp $name:/home/gaoyu/evaluation/hb-zk-3.6.3/logs $1/$name"
    docker cp $name:/home/gaoyu/evaluation/hb-zk-3.6.3/logs $1/$name
    continue
  fi
  if [[ "$name" == "C1hb-hdfs" ]]; then
    echo "$1/$name"
    mkdir $1/$name
    echo "docker cp $name:/home/gaoyu/evaluation/hb-hadoop-3.2.2/logs $1/$name"
    docker cp $name:/home/gaoyu/evaluation/hb-hadoop-3.2.2/logs $1/$name
    continue
  fi
  echo "$1/$name"
  mkdir $1/$name
  echo "docker cp $name:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/$name"
  docker cp $name:/home/gaoyu/evaluation/hbase-2.4.8/logs $1/$name
done