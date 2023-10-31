#!/bin/bash

OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $OWN_DIR/cluster-info.sh

echo "install iptables to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker exec -t $name /bin/bash -ic \"rm /observer/iptables-rules\""
  docker exec -t $name /bin/bash -ic "rm /observer/iptables-rules"
done
