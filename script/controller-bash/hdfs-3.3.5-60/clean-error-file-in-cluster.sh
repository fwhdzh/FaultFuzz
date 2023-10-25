OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
echo "clear error file on cluster..."
for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hd-zk" ]]; then
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hd-zk-3.6.3/core.*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hd-zk-3.6.3/core.*"
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hd-zk-3.6.3/hs_err_*.log*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hd-zk-3.6.3/hs_err_*.log"
    echo "docker exec -t $name /bin/bash -ic rm /core.*"
    docker exec -t $name /bin/bash -ic "rm /core.*"
    echo "docker exec -t $name /bin/bash -ic rm /hs_err_*.log*"
    docker exec -t $name /bin/bash -ic "rm /hs_err_*.log"
  else
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hadoop-3.3.5/core.*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hadoop-3.3.5/core.*"
    echo "docker exec -t $name /bin/bash -ic rm /home/gaoyu/evaluation/hadoop-3.3.5/hs_err_*.log*"
    docker exec -t $name /bin/bash -ic "rm /home/gaoyu/evaluation/hadoop-3.3.5/hs_err_*.log"
    echo "docker exec -t $name /bin/bash -ic rm /core.*"
    docker exec -t $name /bin/bash -ic "rm /core.*"
    echo "docker exec -t $name /bin/bash -ic rm /hs_err_*.log*"
    docker exec -t $name /bin/bash -ic "rm /hs_err_*.log"
  fi
done

