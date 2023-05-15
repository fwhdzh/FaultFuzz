SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker restart C1NN
docker restart C1RM
docker restart C1Master1
docker restart C1Master2
docker restart C1Slave1
docker restart C1Slave2
docker restart C1Slave3
docker restart C1Slave4
docker restart C1HS
docker restart C1WPS
docker restart C1hd-zk

sh $SCRIPT_DIR/waitCleanDocker.sh C1NN
sh $SCRIPT_DIR/waitCleanDocker.sh C1RM
sh $SCRIPT_DIR/waitCleanDocker.sh C1Master1
sh $SCRIPT_DIR/waitCleanDocker.sh C1Master2
sh $SCRIPT_DIR/waitCleanDocker.sh C1Slave1
sh $SCRIPT_DIR/waitCleanDocker.sh C1Slave2
sh $SCRIPT_DIR/waitCleanDocker.sh C1Slave3
sh $SCRIPT_DIR/waitCleanDocker.sh C1Slave4
sh $SCRIPT_DIR/waitCleanDocker.sh C1HS
sh $SCRIPT_DIR/waitCleanDocker.sh C1WPS
sh $SCRIPT_DIR/waitCleanDocker.sh C1hd-zk

