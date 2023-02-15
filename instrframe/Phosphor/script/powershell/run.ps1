docker run --name C1ZK1 -e TZ=Asia/Shanghai --network inst-zoo-net --hostname C1ZK1 --ip 172.21.0.2 -itd fwhdzh/c1zk1
docker run --name C1ZK2 -e TZ=Asia/Shanghai --network inst-zoo-net --hostname C1ZK2 --ip 172.21.0.3 -itd fwhdzh/c1zk2
docker run --name C1ZK3 -e TZ=Asia/Shanghai --network inst-zoo-net --hostname C1ZK3 --ip 172.21.0.4 -itd fwhdzh/c1zk3
docker run --name C1ZK4 -e TZ=Asia/Shanghai --network inst-zoo-net --hostname C1ZK4 --ip 172.21.0.5 -itd fwhdzh/c1zk4
docker run --name C1ZK5 -e TZ=Asia/Shanghai --network inst-zoo-net --hostname C1ZK5 --ip 172.21.0.6 -itd fwhdzh/c1zk5