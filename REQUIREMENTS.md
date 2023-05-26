# Requirements

* A Unix-like operating system (tested on Ubuntu 20)

* A Docker installation

* At least 32GB of RAM (CrashFuzz can start with small RAM, but it
  needs more RAM to handle numerous fault sequences as time goes on)

* At least 50GB of available disk space

* Our experiments were executed using `Java v1.8.0_271` and `OpenJDK
  jdk8u`.

We also provide Docker images for a distributed ZooKeeper cluster with
five nodes, already configured with all dependencies and which is ready-to-use
for this package. The Docker image is hosted at [Docker
Hub](https://hub.docker.com/repository/docker/world4gaoyu/c1zk1,
https://hub.docker.com/repository/docker/world4gaoyu/c1zk2,
https://hub.docker.com/repository/docker/world4gaoyu/c1zk3,
https://hub.docker.com/repository/docker/world4gaoyu/c1zk4,
https://hub.docker.com/repository/docker/world4gaoyu/c1zk5).

More instructions on how to install the dependencies or use the docker image can be found in the `INSTALL.md` file provided in this package.