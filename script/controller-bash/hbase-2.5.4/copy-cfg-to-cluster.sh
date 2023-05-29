OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
CONFIG_DIR=$OWN_DIR/../../../configuration/hbase-2.5.4

source $OWN_DIR/cluster-info.sh

for name in "${clusterName[@]}"
do
  if [[ "$name" == "C1hb-zk" ]]; then
    continue
  fi
  if [[ "$name" == "C1hb-hdfs" ]]; then
    continue
  fi
  echo "docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hbase-2.5.4/conf'"
  docker exec -t $name /bin/bash -ic 'rm -r /home/gaoyu/evaluation/hbase-2.5.4/conf'
  echo "docker cp $CONFIG_DIR/conf $name:/home/gaoyu/evaluation/hbase-2.5.4"
  docker cp $CONFIG_DIR/conf $name:/home/gaoyu/evaluation/hbase-2.5.4
done
