OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

sh $OWN_DIR/restartAllDockers.sh
#docker exec -t C1HM1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1HM2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS3 /bin/bash -ic '/etc/init.d/ssh start'
sh $OWN_DIR/fixHosts.sh

#sh clearRst.sh
sh $OWN_DIR/clearDockerRst.sh
sh $OWN_DIR/clearLogs.sh
sh $OWN_DIR/clearZK.sh

sh $OWN_DIR/clear-checker-process.sh

sh $OWN_DIR/clear-network-condition.sh

sh $OWN_DIR/copy-env-to-cluster.sh

#sh clearRst.sh

