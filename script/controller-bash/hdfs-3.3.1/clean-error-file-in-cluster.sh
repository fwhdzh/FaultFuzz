OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
echo "clear error file on cluster..."
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hd-zk" ]]; then
    echo "no need to clean on C1hd-zk"
  else
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hadoop-3.3.1/core.*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hadoop-3.3.1/core.*"
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hadoop-3.3.1/hs_err_*.log*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hadoop-3.3.1/hs_err_*.log"
  fi
done

