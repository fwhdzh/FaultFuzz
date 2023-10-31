OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# inst_jar_host=/home/fengwenhan/code/instrframe/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar
# inst_jar_host=/home/fengwenhan/code/faultfuzz/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar
inst_jar_host=FaultFuzz-inst-0.0.5-SNAPSHOT.jar
inst_jar_node=/SUT-configuration

docker cp $inst_jar_host C1ZK1:$inst_jar_node
docker cp $inst_jar_host C1ZK2:$inst_jar_node
docker cp $inst_jar_host C1ZK3:$inst_jar_node 
docker cp $inst_jar_host C1ZK4:$inst_jar_node
docker cp $inst_jar_host C1ZK5:$inst_jar_node
