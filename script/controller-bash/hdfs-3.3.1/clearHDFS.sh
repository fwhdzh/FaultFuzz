docker exec -t C1NN /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/*'

docker exec -t C1Master1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/*'

docker exec -t C1Master2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/*'

docker exec -t C1Slave1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/* && rm -rf /home/gaoyu/evaluation/hadoop-3.3.1/journal-local'

docker exec -t C1Slave2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/* && rm -rf /home/gaoyu/evaluation/hadoop-3.3.1/journal-local'

docker exec -t C1Slave3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/* && rm -rf /home/gaoyu/evaluation/hadoop-3.3.1/journal-local'

docker exec -t C1Slave4 /bin/bash -ic 'cd /home/gaoyu/evaluation/hadoop-3.3.1 && sh cleanhdfs.sh && rm -rf /tmp/* && rm -rf /home/gaoyu/evaluation/hadoop-3.3.1/journal-local'
