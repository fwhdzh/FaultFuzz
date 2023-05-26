docker exec -t C1ZK1 /bin/bash -ic 'rm /home/gaoyu/zk381curFault'
docker cp zk381curFault C1ZK1:/home/gaoyu
docker exec -t C1ZK2 /bin/bash -ic 'rm /home/gaoyu/zk381curFault'
docker cp zk381curFault C1ZK2:/home/gaoyu
docker exec -t C1ZK3 /bin/bash -ic 'rm /home/gaoyu/zk381curFault'
docker cp zk381curFault C1ZK3:/home/gaoyu
docker exec -t C1ZK4 /bin/bash -ic 'rm /home/gaoyu/zk381curFault'
docker cp zk381curFault C1ZK4:/home/gaoyu
docker exec -t C1ZK5 /bin/bash -ic 'rm /home/gaoyu/zk381curFault'
docker cp zk381curFault C1ZK5:/home/gaoyu

echo "updateCurFault finish!"
