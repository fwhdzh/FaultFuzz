docker exec -t C1NN /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

#docker exec -it C1RM /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

docker exec -t C1Master1 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

#docker exec -it C1Master2 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

docker exec -t C1Slave1 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

docker exec -t C1Slave2 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

docker exec -t C1Slave3 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

#docker exec -it C1Slave4 /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

#docker exec -it C1HS /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

#docker exec -it C1WPS /bin/bash -ic 'rm -rf /home/gaoyu/dfs331-fav-rst && rm -rf /home/gaoyu/CacheFolder'

docker exec -t C1NN /bin/bash -ic 'rm -rf /home/gaoyu/fuzzcov'
docker exec -t C1Master1 /bin/bash -ic 'rm -rf /home/gaoyu/fuzzcov'
docker exec -t C1Slave1 /bin/bash -ic 'rm -rf /home/gaoyu/fuzzcov'
docker exec -t C1Slave2 /bin/bash -ic 'rm -rf /home/gaoyu/fuzzcov'
docker exec -t C1Slave3 /bin/bash -ic 'rm -rf /home/gaoyu/fuzzcov'
