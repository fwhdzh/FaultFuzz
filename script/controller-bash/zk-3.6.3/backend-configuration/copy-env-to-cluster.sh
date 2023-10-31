OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


docker cp $OWN_DIR/../SUT-configuration/FaultFuzz-SUT-configuration.sh C1ZK1:/SUT-configuration
docker cp $OWN_DIR/../SUT-configuration/FaultFuzz-SUT-configuration.sh C1ZK2:/SUT-configuration
docker cp $OWN_DIR/../SUT-configuration/FaultFuzz-SUT-configuration.sh C1ZK3:/SUT-configuration
docker cp $OWN_DIR/../SUT-configuration/FaultFuzz-SUT-configuration.sh C1ZK4:/SUT-configuration
docker cp $OWN_DIR/../SUT-configuration/FaultFuzz-SUT-configuration.sh C1ZK5:/SUT-configuration
# docker cp $OWN_DIR/fav-env.sh C1ZK2:/zookeeper-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK3:/zookeeper-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK4:/zookeeper-3.6.3 
# docker cp $OWN_DIR/fav-env.sh C1ZK5:/zookeeper-3.6.3 
