#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source $SCRIPT_DIR/cluster-info.sh

echo "install iptables to cluster..."
for name in "${clusterName[@]}"
do
  echo "docker exec -t $name /bin/bash -ic \"rm /home/gaoyu/iptables-rules\""
  docker exec -t $name /bin/bash -ic "rm /home/gaoyu/iptables-rules"
done
