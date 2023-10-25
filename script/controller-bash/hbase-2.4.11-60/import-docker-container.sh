OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

tarFolder=/home/fengwenhan/data/fav-hbase

if [ "${#containerTar[@]}" -ne "${#imageName[@]}" ]
then
    echo "containerTar and imageName have different lengths! the script will exit!"
    exit 1 
fi

for ((i=0; i<${#containerTar[@]}; i++))
do
    # echo "containerTar $i: ${containerTar[i]}"
    # echo "imageName $i: ${imageName[i]}"
    echo "docker import $tarFolder/${containerTar[i]} ${imageName[i]}"
    docker import $tarFolder/${containerTar[i]} ${imageName[i]}
done