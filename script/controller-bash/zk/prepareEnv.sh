SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

sh $SCRIPT_DIR/restartAllDockers.sh
#docker exec -t C1HM1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1HM2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS3 /bin/bash -ic '/etc/init.d/ssh start'
sh $SCRIPT_DIR/fixHosts.sh

#sh clearRst.sh
sh $SCRIPT_DIR/clearDockerRst.sh
sh $SCRIPT_DIR/clearLogs.sh
sh $SCRIPT_DIR/clearZK.sh

sh $SCRIPT_DIR/clear-checker-process.sh

#sh clearRst.sh

