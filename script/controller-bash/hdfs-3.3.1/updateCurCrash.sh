docker exec -t C1NN /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
docker cp dfs331curCrash C1NN:/home/gaoyu
docker exec -t C1Master1 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
docker cp dfs331curCrash C1Master1:/home/gaoyu
#docker exec -t C1Master2 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
#docker cp dfs331curCrash C1Master2:/home/gaoyu
docker exec -t C1Slave1 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
docker cp dfs331curCrash C1Slave1:/home/gaoyu
docker exec -t C1Slave2 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
docker cp dfs331curCrash C1Slave2:/home/gaoyu
docker exec -t C1Slave3 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
docker cp dfs331curCrash C1Slave3:/home/gaoyu
#docker exec -t C1Slave4 /bin/bash -ic 'rm /home/gaoyu/dfs331curCrash'
#docker cp dfs331curCrash C1Slave4:/home/gaoyu
