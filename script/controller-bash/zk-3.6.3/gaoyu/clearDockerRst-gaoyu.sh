#sh clearZK.sh

docker exec -t C1ZK1 /bin/bash -ic 'rm -r /home/gaoyu/CacheFolder'
docker exec -t C1ZK1 /bin/bash -ic 'rm -r /home/gaoyu/zk363-fav-rst'
#docker exec -t C1ZK1 /bin/bash -ic 'rm -r /home/gaoyu/jacoco.exec'
docker exec -t C1ZK2 /bin/bash -ic 'rm -r /home/gaoyu/zk363-fav-rst'
docker exec -t C1ZK2 /bin/bash -ic 'rm -r /home/gaoyu/CacheFolder'
#docker exec -t C1ZK2 /bin/bash -ic 'rm -r /home/gaoyu/jacoco.exec'
docker exec -t C1ZK3 /bin/bash -ic 'rm -r /home/gaoyu/zk363-fav-rst'
docker exec -t C1ZK3 /bin/bash -ic 'rm -r /home/gaoyu/CacheFolder'
#docker exec -t C1ZK3 /bin/bash -ic 'rm -r /home/gaoyu/jacoco.exec'
docker exec -t C1ZK4 /bin/bash -ic 'rm -r /home/gaoyu/zk363-fav-rst'
docker exec -t C1ZK4 /bin/bash -ic 'rm -r /home/gaoyu/CacheFolder'
#docker exec -t C1ZK4 /bin/bash -ic 'rm -r /home/gaoyu/jacoco.exec'
docker exec -t C1ZK5 /bin/bash -ic 'rm -r /home/gaoyu/zk363-fav-rst'
docker exec -t C1ZK5 /bin/bash -ic 'rm -r /home/gaoyu/CacheFolder'
#docker exec -t C1ZK5 /bin/bash -ic 'rm -r /home/gaoyu/jacoco.exec'

#docker exec -t C1ZK1 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov && rm -r /home/gaoyu/fuzzcov-recs'
#docker exec -t C1ZK2 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov && rm -r /home/gaoyu/fuzzcov-recs'
#docker exec -t C1ZK3 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov && rm -r /home/gaoyu/fuzzcov-recs'
#docker exec -t C1ZK4 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov && rm -r /home/gaoyu/fuzzcov-recs'
#docker exec -t C1ZK5 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov && rm -r /home/gaoyu/fuzzcov-recs'

docker exec -t C1ZK1 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov'
docker exec -t C1ZK2 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov'
docker exec -t C1ZK3 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov'
docker exec -t C1ZK4 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov'
docker exec -t C1ZK5 /bin/bash -ic 'rm -r /home/gaoyu/fuzzcov'
