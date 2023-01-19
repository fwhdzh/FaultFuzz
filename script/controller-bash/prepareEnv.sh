sh restartAllDockers.sh
#docker exec -t C1HM1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1HM2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS1 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS2 /bin/bash -ic '/etc/init.d/ssh start'
#docker exec -t C1RS3 /bin/bash -ic '/etc/init.d/ssh start'
sh fixHosts.sh

#sh clearRst.sh
sh clearDockerRst.sh
sh clearLogs.sh
sh clearZK.sh

#sh clearRst.sh
