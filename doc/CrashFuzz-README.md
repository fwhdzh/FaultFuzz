# FaultFuzz: Coverage Guided Fault Injection for Distributed Systems

The repository contains the instructions and scripts to re-run the evaluation
described in our paper. The repository has the following structure:

* `script/`: This is the directory that contains the scripts needed to
  re-run the experiments presented in our paper.
* `inst/`: The source code of `FaultFuzz` used for
  instrumenting the target system.
* `ctrl/`: The source code of `FaultFuzz` used for remaining
  logics.
* `workload/`: Contains the workloads used in our paper
  for testing ZooKeeper, HBase and HDFS.

Inside the `scripts` directory, there are the following directories
* `zk-3.6.3/`: Some helper scripts for running `FaultFuzz` for
  ZooKeeper v3.6.3 or setting up the environment via Docker.
* `hbase-2.4.8/`: Some helper scripts for running `FaultFuzz` for
  HBase v2.4.8 or setting up the environment via Docker.
* `hdfs-3.3.1/`: Some helper scripts for running `FaultFuzz` for
  HDFS v3.3.1 or setting up the environment via Docker.
* `zk-3.8.1/`: Some helper scripts for running `FaultFuzz` for
  ZooKeeper v3.8.1 or setting up the environment via Docker.
* `hdfs-3.3.5/`: Some helper scripts for running `FaultFuzz` for
  HDFS v3.3.5 or setting up the environment via Docker.
* `hbase-2.4.11/`: Some helper scripts for running `FaultFuzz` for
  HBase v2.4.11 or setting up the environment via Docker.


Note that `FaultFuzz` is available as open-source software under the
Apache License 2.0, and can also be reached through the following
Repository : https://github.com/tcse-iscas/FaultFuzz.

For any additional information, contact the first author by e-mail:
Dr. Wenhan Feng \<fengwenhan21@otcaix.iscas.ac.cn\>

## Requirements

See [REQUIREMENTS.md](./REQUIREMENTS.md)

## Setup

See [INSTALL.md](./INSTALL.md)

## Getting Started

### Build Target Cluster

To get started with `FaultFuzz`, we should build a distributed cluster
for the target system (i.e., ZooKeeper, HBase or HDFS) with Docker in
a host machine. We provide Docker images for a distributed ZooKeeper cluster with
five nodes as an example. The Docker image is hosted at Docker
Hub (See ./INSTALL.md).

Then we should generate an instrumented version of the runtime
environment in every node of the target cluster. We can run the command 
`java -jar FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava <jre_path> <output_path>`
to prepare an instrumented JRE.

```
-forJava: Specify the instrumentation for JRE.
jre_path: Path for the input JRE.
output_path: Path for the instrumented JRE.
```

We can configure every node of the target system to use the
instrumented JRE and include the FaultFuzz as the Java agent with a
JVM argument for run time instrumentation. Take ZooKeeper as an
example. We can modify zkEnv.sh file and add following configuration:

```
JAVA = <instrumented_jre_path>/bin/java 
FAULTFUZZ_JVMFLAGS =
-Xbootclasspath/a:<FAULTFUZZ_path>/Phosphor-0.0.5-SNAPSHOT.jar
-javaagent:<FaultFuzz_path>/Phosphor-0.0.5-SNAPSHOT.jar
=useFaultFuzz=true,forZk=true,
jdkFile=true,recordPath=<trace_path>,recordPath=<io_path>,covPath=<coverage_path>,
currentFault=<current_fault_sequence_path>,controllerSocket=<host_ip:controller_port_number>,aflPort=<port_number_for_receiving_record_coverage_info>
```

The parameters are explained as follows: 

```
useFaultFuzz: true for using FaultFuzz.
forZk: true for tracking ZooKeeper socket messages.
jdkFile: true for tracking reads/writes to local files.
```

### Run FaultFuzz

Then we should write a property file for the target system to specify
required paths and scripts, e.g., zk.properties. In the property file,
we should specify:

```
* WORKLOAD: The string path for the script used for running a workload.
* CRASH: The string path for the script used for crash a node in the
  target system according to the ip address.
* REBOOT: The string path for the script used for reboot a crash
  node according to the ip address.
* NETWORK_DISCONNECTION: The string path for the script used for disconnect 
  netword between two nodes according to the ip addresses.
* NETWORK_RECONNECTION: The string path for the script used for reconnect 
  netword between two nodes according to the ip addresses.
* PRETREATMENT: The string path for the script used for prepare a
  clean environment for the target cluster before every fault
  injection test, e.g., remove stale data from last test.
* CHECKER: The string path for the script used for checking failure symptoms.
* MONITOR: The string path for the script used for collecting runtime
  information from the target cluster.
* CUR_FAULT_FILE: The string path for the file used for storing
  current fault sequence under test.
* UPDATE_FAULT: The string path for the script used for copying
  CUR_FAULT_FILE to each node of the target cluster.
* ROOT_DIR: The string path for the directory used for storing test
  outputs.
* TEST_TIME: To specify the test time.
* MAX_FAULTS: To specify the maximum number of faults in a fault sequence.
* FAULT_CSTR: To specify system-specify constraints, i.e., the number 
  of dead nodes should not exceed the maximum number of dead nodes that
  the target system can tolerate. For example,
  `2:{172.30.0.2,172.30.0.3,172.30.0.4,172.30.0.5,172.30.0.6}` means for
  a distributed cluster with five nodes (`172.30.0.2, 172.30.0.3,
  172.30.0.4, 172.30.0.5 and 172.30.0.6`), 
  it can tolerate the simultaneous downtime of two nodes at most.
* AFL_PORT: To specify the port number in a target cluster used for
  receiving the command from the controller to record coverage
  information.
* HANG_TIMEOUT: To specify the timeout period that used for confirming a
  hang bug.
* DETERMINE_WAIT_TIME: To specify the timeout period that used for 
  giving up to wait next I/O operation.
```

We should customize scripts used in the property file for every target
system and workload. We show the scripts used in our evaluation in
`scripts/` directory.

Then, we can run the following command to start FaultFuzz:

```
java -cp FaultFuzz-0.0.1-SNAPSHOT.jar edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain <controller_port_number> <property_file_path>
```

**NOTE**: Distributed systems run with nondeterminism, and FaultFuzz
combines random factors when selecting a fault sequence to test.
Therefore, we may not get exactly the same results as the experiment
in our paper.

## Example

We show an example to run FaultFuzz on ZooKeeper. You can follow
these steps:

1. Install FaultFuzz. See [INSTALL.md](./INSTALL.md)
2. Pull images from Dockerhub. See [INSTALL.md](./INSTALL.md)
3. Using the Docker images. See [INSTALL.md](./INSTALL.md)
4. Run FaultFuzz. See [INSTALL.md](./INSTALL.md)

We can check outputs of FaultFuzz on #ROOT_DIR#.

## Found Bugs

FaultFuzz has found five crash recovery bugs in three widely-used
cloud systems including [ZooKeeper v3.6.3](https://zookeeper.apache.org/), [HDFS v3.3.1](https://hadoop.apache.org/) 
and [HBase v2.4.6](https://hbase.apache.org/). 
The following table shows the bugs detected by FaultFuzz in these
systems for now.

| Bug ID                                                                 |     Failure Symptom      |
| ---------------------------------------------------------------------- | :----------------------: |
| [HBASE-26883](https://issues.apache.org/jira/browse/HBASE-26883)       |        Data loss         |
| [ZOOKEEPER-4503](https://issues.apache.org/jira/browse/ZOOKEEPER-4503) |      Data staleness      |
| [HBASE-26897](https://issues.apache.org/jira/browse/HBASE-26897)       |  Cluster out of service  |
| [HBASE-26370](https://issues.apache.org/jira/browse/HBASE-26370)       | Misleading error message |
| [HDFS-16508](https://issues.apache.org/jira/browse/HDFS-16508)         |    Operation failure     |