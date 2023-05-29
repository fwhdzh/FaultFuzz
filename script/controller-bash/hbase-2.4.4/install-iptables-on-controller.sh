OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

echo "install iptables to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker exec -t $name /bin/bash -ic \"apt-get install iptables -y\""
  docker exec -t $name /bin/bash -ic "apt-get install iptables -y"
done
