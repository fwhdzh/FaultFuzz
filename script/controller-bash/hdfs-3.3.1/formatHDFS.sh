OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
sh $OWN_DIR/clearHDFS.sh
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --workers --daemon start journalnode && jps'
docker exec -t C1NN /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.1/bin/hdfs namenode -format mycluster && jps'
docker exec -t C1NN /bin/bash -ic '/home/gaoyu/evaluation/hadoop-3.3.1/bin/hdfs zkfc -formatZK -force && jps'
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start namenode && jps'
docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs namenode -bootstrapStandby -force && jps'
docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start namenode && jps'
docker exec -t C1Master2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs namenode -bootstrapStandby -force && jps'
docker exec -t C1Master2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --daemon start namenode && jps'
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --workers --hostnames "C1NN C1Master1 C1Master2" --daemon start zkfc'
docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1/ && bin/hdfs --workers --daemon start datanode'
