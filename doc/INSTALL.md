## Environment Setup

We provide docker images to build a ZooKeeper cluster for FaultFuzz.
And we use ZooKeeper as an example to run FaultFuzz.

### Install FaultFuzz

First, please clone the project to your computer using Git, and then navigate to its main directory.

```bash
git clone https://github.com/tcse-iscas/FaultFuzz.git
cd faultfuzz
```

FaultFuzz is built based on Maven. To install it, please use the following command.

```bash
mvn install
```

We provide a `package.sh` script to put configurations, jar package and scripts together. Please use the following command:
```bash
./package.sh zk-3.6.3
cd ./package/zk-3.6.3
```


Execute `chmod 777 <file.sh>` for the scripts in the
`package/zk-3.6.3` directory.

### Pull Images from Dockerhub

You can download the Docker images from Dockerhub by using the
following commands

```bash
docker pull fwhdzh/c1zk1
# Rename the image to be consistent with our scripts
docker tag fwhdzh/c1zk1 c1zk1

docker pull fwhdzh/c1zk2
# Rename the image to be consistent with our scripts
docker tag fwhdzh/c1zk2 c1zk2

docker pull fwhdzh/c1zk3
# Rename the image to be consistent with our scripts
docker tag fwhdzh/c1zk3 c1zk3

docker pull fwhdzh/c1zk4
# Rename the image to be consistent with our scripts
docker tag fwhdzh/c1zk4 c1zk4

docker pull fwhdzh/c1zk5
# Rename the image to be consistent with our scripts
docker tag fwhdzh/c1zk5 c1zk5
```

### Using the Docker Images

Use docker images to build a ZooKeeper cluster with five nodes
(execute as a root user):

```bash
docker-compose -p fav-zk -f zk-compose.yaml up -d
```

Note that before executing this command, make sure you do not have a docker network which is named as fav-zookeeper1 and the IP range 172.30.0.0/16 is not occupied.

If there is a firewall in your machine, the following commands may help.
Just make sure the host machine can communicate with the nodes in the ZooKeeper cluster.

```
firewall-cmd --permanent --zone=public --add-rich-rule='rule family=ipv4 source address=172.30.0.0/16 accept'
firewall-cmd --reload
```

Append the following content to `/etc/hosts` and make sure the host machine can identify the nodes in the ZooKeeper cluster.

```
172.30.0.2      C1ZK1 C1ZK1.fav-zookeeper1
172.30.0.3      C1ZK2 C1ZK2.fav-zookeeper1
172.30.0.4      C1ZK3 C1ZK3.fav-zookeeper1
172.30.0.5      C1ZK4 C1ZK4.fav-zookeeper1
172.30.0.6      C1ZK5 C1ZK5.fav-zookeeper1
```

<!-- If you want to use other network segment like 172.40.0.0:16, you should also update all ip addresses in scripts. -->

## Testing the Setup

Run the following commands to start FaultFuzz:

```bash
sh faultfuzz.sh
```

**NOTE**: FaultFuzz logs information about the start of a fault injection test, the injection of a node crash/reboot, observed failure symptoms, potential bugs and so on. For example, `Going to conduct test *` means a new fault injection test is started. `Find a BUG for test *` means a potential bug is triggered. `INFO - Prepare to crash node *` means FaultFuzz is going to inject a node crash fault. Note that some errors and exceptions may show up when running FaultFuzz, and this may not mean that FaultFuzz runs incorrectly. For example, after crashing a ZooKeeper node, we may encounter the container not running error when checking the status of the target system. FaultFuzz users need a certain understanding of the target distributed system to decide whether the errors and exceptions are expected.
