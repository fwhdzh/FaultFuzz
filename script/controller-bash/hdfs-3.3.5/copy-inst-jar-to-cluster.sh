OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# inst_jar_host=/home/fengwenhan/code/instrframe/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar
# inst_jar_host=/home/fengwenhan/code/faultfuzz/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar
inst_jar_host=FaultFuzz-inst-0.0.5-SNAPSHOT.jar
inst_jar_node=/home/gaoyu

source $OWN_DIR/cluster-info.sh

echo "copy inst-jar to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker cp $inst_jar_host $name:$inst_jar_node"
  docker cp $inst_jar_host $name:$inst_jar_node
done
