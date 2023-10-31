#sh clearZK.sh

observerHome=/observer

observerRecordPath=$observerHome/fav-rst
observerCacheDir=$observerHome/CacheFolder
observerCovPath=$observerHome/fuzzcov

docker exec -t C1ZK1 /bin/bash -ic "rm -r $observerRecordPath"
docker exec -t C1ZK1 /bin/bash -ic "rm -r $observerCacheDir"
docker exec -t C1ZK2 /bin/bash -ic "rm -r $observerRecordPath"
docker exec -t C1ZK2 /bin/bash -ic "rm -r $observerCacheDir"
docker exec -t C1ZK3 /bin/bash -ic "rm -r $observerRecordPath"
docker exec -t C1ZK3 /bin/bash -ic "rm -r $observerCacheDir"
docker exec -t C1ZK4 /bin/bash -ic "rm -r $observerRecordPath"
docker exec -t C1ZK4 /bin/bash -ic "rm -r $observerCacheDir"
docker exec -t C1ZK5 /bin/bash -ic "rm -r $observerRecordPath"
docker exec -t C1ZK5 /bin/bash -ic "rm -r $observerCacheDir"



docker exec -t C1ZK1 /bin/bash -ic "rm -r $observerCovPath"
docker exec -t C1ZK2 /bin/bash -ic "rm -r $observerCovPath"
docker exec -t C1ZK3 /bin/bash -ic "rm -r $observerCovPath"
docker exec -t C1ZK4 /bin/bash -ic "rm -r $observerCovPath"
docker exec -t C1ZK5 /bin/bash -ic "rm -r $observerCovPath"
