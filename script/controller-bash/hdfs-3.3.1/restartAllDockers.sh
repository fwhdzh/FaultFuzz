OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

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

sh $OWN_DIR/waitCleanDocker.sh C1NN
sh $OWN_DIR/waitCleanDocker.sh C1RM
sh $OWN_DIR/waitCleanDocker.sh C1Master1
sh $OWN_DIR/waitCleanDocker.sh C1Master2
sh $OWN_DIR/waitCleanDocker.sh C1Slave1
sh $OWN_DIR/waitCleanDocker.sh C1Slave2
sh $OWN_DIR/waitCleanDocker.sh C1Slave3
sh $OWN_DIR/waitCleanDocker.sh C1Slave4
sh $OWN_DIR/waitCleanDocker.sh C1HS
sh $OWN_DIR/waitCleanDocker.sh C1WPS
sh $OWN_DIR/waitCleanDocker.sh C1hd-zk

