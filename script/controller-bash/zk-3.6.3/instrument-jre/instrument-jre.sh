OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

# 循环遍历数组，对每个元素执行命令
echo "instrument jre to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker exec -t $name /bin/bash -ic \"cd /SUT-configuration/ && . buildJRE.sh \""
  docker exec -t $name /bin/bash -ic "cd /SUT-configuration/ && . buildJRE.sh "
done

