#docker exec -t WPS /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp  &&     rm -rf logs'

docker exec -t C1HM1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp/* && rm -rf logs/* && rm -rf zkData/version-2'

docker exec -t C1HM2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp/* && rm -rf logs/* && rm -rf zkData/version-2'

docker exec -t C1RS1 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp/* && rm -rf logs/* && rm -rf zkData/version-2'

docker exec -t C1RS2 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp/* && rm -rf logs/* && rm -rf zkData/version-2'

docker exec -t C1RS3 /bin/bash -ic 'cd /home/gaoyu/evaluation/hbase-2.4.8 && rm -rf tmp/* && rm -rf logs/* && rm -rf zkData/version-2'
