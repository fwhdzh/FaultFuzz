# docker export -o /home/fengwenhan/fav-zk/C1ZK1.tar C1NN
# docker export -o /home/fengwenhan/fav-hdfs/C1RM.tar C1RM
# docker export -o /home/fengwenhan/fav-hdfs/C1Master1.tar C1Master1
# docker export -o /home/fengwenhan/fav-hdfs/C1Master2.tar C1Master2
# docker export -o /home/fengwenhan/fav-hdfs/C1Slave1.tar C1Slave1
# docker export -o /home/fengwenhan/fav-hdfs/C1Slave2.tar C1Slave2
# docker export -o /home/fengwenhan/fav-hdfs/C1Slave3.tar C1Slave3
# docker export -o /home/fengwenhan/fav-hdfs/C1Slave4.tar C1Slave4
# docker export -o /home/fengwenhan/fav-hdfs/C1HS.tar C1HS
# docker export -o /home/fengwenhan/fav-hdfs/C1WPS.tar C1WPS
# docker export -o /home/fengwenhan/fav-hdfs/C1hd-zk.tar C1hd-zk

OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

tarFolder=/home/fengwenhan/data/fav-zk

if [ "${#containerTar[@]}" -ne "${#imageName[@]}" ]
then
    echo "containerTar and imageName have different lengths! the script will exit!"
    exit 1 
fi

for ((i=0; i<${#containerTar[@]}; i++))
do
    echo "docker export -o  $tarFolder/${containerTar[i]} ${clusterName[i]}"
    docker export -o  $tarFolder/${containerTar[i]} ${clusterName[i]}
done