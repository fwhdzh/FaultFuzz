read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $SCRIPT_DIR/cluster-info.sh

echo "docker-compose -p fav-zk -f ~/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1/zk-compose.yaml down"
docker-compose -p fav-zk -f ~/code/crashfuzz-ctrl/script/controller-bash/zk-3.8.1/zk-compose.yaml down

echo "rm images of cluster..."
for name in "${imageName[@]}"
do
  echo "docker image rm $name"
  docker image rm $name
done

