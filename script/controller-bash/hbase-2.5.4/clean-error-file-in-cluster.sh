OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
echo "clear error file on cluster..."
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hb-zk" ]]; then
    echo "no need to clean on C1hb-zl"
    continue
  fi
  if [[ "$name" == "C1hb-hdfs" ]]; then
    echo "no need to clean on C1hb-hdfs"
    continue
  fi
  echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hbase-2.5.4/core.*"
  docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hbase-2.5.4/core.*"
  echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hbase-2.5.4/hs_err_*.log*"
  docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hbase-2.5.4/hs_err_*.log"
done

