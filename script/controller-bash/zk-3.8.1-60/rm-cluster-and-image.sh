read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac

OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh
# source $OWN_DIR/configuration.sh

echo "docker-compose -p fav-zk -f zk-compose.yaml down"
docker-compose -p fav-zk -f zk-compose.yaml down

echo "rm images of cluster..."
for name in "${imageName[@]}"
do
  echo "docker image rm $name"
  docker image rm $name
done

