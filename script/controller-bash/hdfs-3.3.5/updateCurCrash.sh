docker exec -t C1NN /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
docker cp dfs331curFault C1NN:/home/gaoyu
docker exec -t C1Master1 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
docker cp dfs331curFault C1Master1:/home/gaoyu
#docker exec -t C1Master2 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
#docker cp dfs331curFault C1Master2:/home/gaoyu
docker exec -t C1Slave1 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
docker cp dfs331curFault C1Slave1:/home/gaoyu
docker exec -t C1Slave2 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
docker cp dfs331curFault C1Slave2:/home/gaoyu
docker exec -t C1Slave3 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
docker cp dfs331curFault C1Slave3:/home/gaoyu
#docker exec -t C1Slave4 /bin/bash -ic 'rm /home/gaoyu/dfs331curFault'
#docker cp dfs331curFault C1Slave4:/home/gaoyu
