docker exec -t C1ZK1 /bin/bash -ic "rm /home/gaoyu/CacheFolder/* "
docker exec -t C1ZK2 /bin/bash -ic "rm /home/gaoyu/CacheFolder/* "
docker exec -t C1ZK3 /bin/bash -ic "rm /home/gaoyu/CacheFolder/* "
docker exec -t C1ZK4 /bin/bash -ic "rm /home/gaoyu/CacheFolder/* "
docker exec -t C1ZK5 /bin/bash -ic "rm /home/gaoyu/CacheFolder/* "

docker exec -t C1ZK1 /bin/bash -ic ". /home/gaoyu/buildJRE.sh "
docker exec -t C1ZK2 /bin/bash -ic ". /home/gaoyu/buildJRE.sh "
docker exec -t C1ZK3 /bin/bash -ic ". /home/gaoyu/buildJRE.sh "
docker exec -t C1ZK4 /bin/bash -ic ". /home/gaoyu/buildJRE.sh "
docker exec -t C1ZK5 /bin/bash -ic ". /home/gaoyu/buildJRE.sh "