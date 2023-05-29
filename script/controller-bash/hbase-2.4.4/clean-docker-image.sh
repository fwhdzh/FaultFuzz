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

OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

echo "rm images of cluster..."
for name in "${imageName[@]}"
do
  echo "docker image rm $name"
  docker image rm $name
done

