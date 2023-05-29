echo "***************Current ZK Info*************"
echo "*******get active namenode:"
docker exec -t C1hd-zk /bin/bash -ic 'cd /home/gaoyu/evaluation/hd-zk-3.6.3 && bin/zkCli.sh -server localhost:11181 get /hadoop-ha/mycluster/ActiveBreadCrumb > tmpout.txt && tail -n 5 tmpout.txt'
docker exec -t C1hd-zk /bin/bash -ic "cd /home/gaoyu/evaluation/hd-zk-3.6.3 && rm tmpout.txt"

