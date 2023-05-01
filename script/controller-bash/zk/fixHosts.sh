docker cp zkhosts C1ZK1:/etc/
docker exec -t C1ZK1 /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

docker cp zkhosts C1ZK2:/etc/
docker exec -t C1ZK2 /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

docker cp zkhosts C1ZK3:/etc/
docker exec -t C1ZK3 /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

docker cp zkhosts C1ZK4:/etc/
docker exec -t C1ZK4 /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

docker cp zkhosts C1ZK5:/etc/
docker exec -t C1ZK5 /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"

#docker cp zkhosts C1ZK-cli:/etc/
#docker exec -t C1ZK-cli /bin/bash -ic "cat /etc/zkhosts >> /etc/hosts"
