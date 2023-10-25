echo "***************Current ZK Info*************"
echo "********ls /hbase"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase/backup-masters"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase/backup-masters > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase/master"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase/master > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase/running"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase/running > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase/meta-region-server"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase/meta-region-server > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase/rs"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase/rs > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase/table"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase/table > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase/splitWAL"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase/splitWAL> tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

#echo "********ls /hbase/table-lock"
#docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase/table-lock > tmpout.txt && tail -n 5 tmpout.txt"
#docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"
