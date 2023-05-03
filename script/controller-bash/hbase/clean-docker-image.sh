read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac


# docker image rm fwhdzh/c1hm1
# docker image rm fwhdzh/c1hm2
# docker image rm fwhdzh/c1rs1
# docker image rm fwhdzh/c1rs2
# docker image rm fwhdzh/c1rs3
# docker image rm fwhdzh/c1hb-hdfs
# docker image rm fwhdzh/c1hb-zk

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $SCRIPT_DIR/cluster-info.sh

for ((i=0; i<${#imageName[@]}; i++))
do
    echo "docker image rm $imageName"
    docker image rm $imageName
done