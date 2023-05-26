read -p "Are you sure you want to execute this script? [y/n] " choice
case "$choice" in
  y|Y ) echo "Yes, executing script...";;
  n|N ) echo "No, exiting script..."; exit;;
  * ) echo "Invalid choice, exiting script..."; exit;;
esac


echo "docker-compose -p fav-hdfs -f ~/code/faultfuzz/script/controller-bash/hdfs-3.3.5-60/hdfs-compose.yaml down"
docker-compose -p fav-hdfs -f ~/code/faultfuzz/script/controller-bash/hdfs-3.3.5-60/hdfs-compose.yaml down

echo "docker image rm fwhdzh/c1nn"
docker image rm fwhdzh/c1nn

echo "docker image rm fwhdzh/c1rm"
docker image rm fwhdzh/c1rm

echo "docker image rm fwhdzh/c1master1"
docker image rm fwhdzh/c1master1

echo "docker image rm fwhdzh/c1master2"
docker image rm fwhdzh/c1master2

echo "docker image rm fwhdzh/c1slave1"
docker image rm fwhdzh/c1slave1

echo "docker image rm fwhdzh/c1slave2"
docker image rm fwhdzh/c1slave2

echo "docker image rm fwhdzh/c1slave3"
docker image rm fwhdzh/c1slave3

echo "docker image rm fwhdzh/c1slave4"
docker image rm fwhdzh/c1slave4

echo "docker image rm fwhdzh/c1hs"
docker image rm fwhdzh/c1hs

echo "docker image rm fwhdzh/c1wps"
docker image rm fwhdzh/c1wps

echo "docker image rm fwhdzh/c1hd-zk"
docker image rm fwhdzh/c1hd-zk
