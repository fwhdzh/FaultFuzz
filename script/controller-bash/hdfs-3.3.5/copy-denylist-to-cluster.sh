SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# denylist_host=/home/fengwenhan/code/instrframe/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar
denylist_host=$SCRIPT_DIR/denylist
denylist_node=/home/gaoyu/evaluation/hadoop-3.3.5

source $SCRIPT_DIR/cluster-info.sh

echo "copy denylist to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker cp $denylist_host $name:$denylist_node"
  docker cp $denylist_host $name:$denylist_node
done
