OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# CONFIG_DIR=$OWN_DIR/../../../configuration/hdfs
CONFIG_DIR=$OWN_DIR/configuration

source $OWN_DIR/cluster-info.sh

for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hd-zk" ]]; then
    continue
  fi
  echo "docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hadoop-3.3.5/etc'"
  docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hadoop-3.3.5/etc'
  echo "docker cp $CONFIG_DIR/etc $name:/home/gaoyu/evaluation/hadoop-3.3.5/etc"
  docker cp $CONFIG_DIR/etc $name:/home/gaoyu/evaluation/hadoop-3.3.5/etc
done
