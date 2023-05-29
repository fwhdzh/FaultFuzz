echo "***************Current ZK Info*************"
echo "********ls /hbase"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase-2.4.4/backup-masters"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase-2.4.4/backup-masters > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase-2.4.4/master"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase-2.4.4/master > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase-2.4.4/running"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase-2.4.4/running > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase-2.4.4/meta-region-server"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase-2.4.4/meta-region-server > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase-2.4.4/rs"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase-2.4.4/rs > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********ls /hbase-2.4.4/table"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase-2.4.4/table > tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

echo "********get /hbase-2.4.4/splitWAL"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hbase-2.4.4/splitWAL> tmpout.txt && tail -n 5 tmpout.txt"
docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"

#echo "********ls /hbase-2.4.4/table-lock"
#docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 ls /hbase-2.4.4/table-lock > tmpout.txt && tail -n 5 tmpout.txt"
#docker exec -t C1hb-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hb-zk-3.6.3 && rm tmpout.txt"
