OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
sh $OWN_DIR/clearHDFS.sh
docker exec -it C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.5/ && bin/hdfs --workers --daemon start journalnode && jps'
docker exec -it C1NN /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.5/bin/hdfs namenode -format mycluster && jps'
docker exec -it C1NN /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.5/bin/hdfs zkfc -formatZK -force && jps'