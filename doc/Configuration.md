Note that the configurations in the
configuration file are more low-level and slightly different from the configuration items
provided in our FaultFuzz frontend website. In the FaultFuzz frontend website, we have
simplified the configuration information to make it more human-understandable.

There are two configuration files used in FaultFuzz, i.e., configuration file for FaultFuzz backend
(named FaultFuzz-backend-configuration.properties if you generate it by FaultFuzz frontend) and configuration file 
for FaultFuzz observer and the system under test (named FaultFuzz-SUT-configuration.sh if you generate it by FaultFuzz frontend).

FaultFuzz-backend-configuration.properties is property file, which looks like: 
```
WORKLOAD={/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/workload-1.sh,/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/workload-2.sh}
CHECKER=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/detectFailureSymptoms.sh
FAULT_TYPE=[CRASH,REBOOT,NETWORK_DISCONNECTION,NETWORK_RECONNECTION]
CRASH=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/crashNode.sh
REBOOT=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/startNode.sh
NETWORK_DISCONNECTION=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/network-disconnect.sh
NETWORK_RECONNECTION=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/network-connect.sh
ROOT_DIR=/data/faultfuzz_zk
CUR_FAULT_FILE=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/faultUnderTest
CONTROLLER_PORT=12090
MONITOR=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/copyRuntimeInformation.sh
PRETREATMENT=/zookeeper/faultfuzz/package/zk-3.6.3/backend-configuration/resetSystem.sh
TEST_TIME=80h
FAULT_CSTR=2:{172.30.0.2,172.30.0.3,172.30.0.4,172.30.0.5,172.30.0.6}
AFL_PORT=12081
HANG_TIMEOUT=10m
MAX_FAULTS=10
DETERMINE_WAIT_TIME=30000
```

The configuration options supported by FaultFuzz-backend-configuration.properties include:

- **WORKLOAD**: The string path for the script used for running a workload.
- **CHECKER**: The string path for the script used for checking failure symptoms.

- **FAULT_TYPE**: The fault types to be injected to the systems. E.g., [CRASH,NETWORK_DISCONNECTIOn]
- **CRASH**: The string path for the script used for crash a node in the
  target system according to the ip address.
- **REBOOT**: The string path for the script used for reboot a crash
  node according to the ip address.
- **NETWORK_DISCONNECTION**: The string path for the script used for disconnect 
  netword between two nodes according to the ip addresses.
- **NETWORK_RECONNECTION**: The string path for the script used for reconnect 
  netword between two nodes according to the ip addresses.

- **MAX_FAULTS**: To specify the maximum number of faults in a fault sequence.
- **FAULT_CSTR**: To specify system-specify constraints, i.e., the number 
  of dead nodes should not exceed the maximum number of dead nodes that
  the target system can tolerate. For example,
  `2:{172.30.0.2,172.30.0.3,172.30.0.4,172.30.0.5,172.30.0.6}` means for
  a distributed cluster with five nodes (`172.30.0.2, 172.30.0.3,
  172.30.0.4, 172.30.0.5 and 172.30.0.6`), 
  it can tolerate the simultaneous downtime of two nodes at most.
- **AFL_PORT**: To specify the port number in a target cluster used for
  receiving the command from the controller to record coverage
  information.

- **MONITOR**: The string path for the script used for collecting runtime
  information from the target cluster.
- **PRETREATMENT**: The string path for the script used for prepare a
  clean environment for the target cluster before every fault
  injection test, e.g., remove stale data from last test.

- **ROOT_DIR**: The string path for the directory used for storing test
  outputs.
- **CUR_FAULT_FILE**: The string path for the file used for storing
  current fault sequence under test.

- **TEST_TIME**: To specify the test time.
- **HANG_TIMEOUT**: To specify the timeout period that used for confirming a
  hang bug.
- **DETERMINE_WAIT_TIME**: To specify the timeout period that used for 
  giving up to wait next I/O operation.


FaultFuzz-SUT-configuration.sh is a linux bash file. It provides two Linux environment variables, i.e., `FAV_OPTS` and `PHOS_OPTS`. 
Where FAV_OPTS is an environment variable generated based on the user-defined
configuration. PHOS_OPTS, on the other hand, retains additional information
introduced by FaultFuzz on top of FAV_OPTS, but without control. PHOS_OPTS is
particularly useful in distributed systems where multiple processes interact.
For instance, in the case of Zookeeper, we may want the server processes to be
controlled by FaultFuzz while the client processes remain uncontrolled.

An example of FaultFuzz-SUT-configuration.sh looks like:

```
export PHOS_OPTS="-Xbootclasspath/a:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false"

export FAV_OPTS="-Xbootclasspath/a:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,forZk=true,jdkFile=true,observerHome=/observer,dataPaths=/zookeeper-3.6.3/zkData/version-2,controllerSocket=172.30.0.1:12090,covIncludes=org/apache/zookeeper,aflAllow=/SUT-configuration/allowlist,aflDeny=/SUT-configuration/denylist,aflPort=12081"
```

The configuration options supported by FaultFuzz-SUT-configuration.sh include:

- **useFaultFuzz**: Controls the usage of FaultFuzz for the program or process.

- **controllerSocket**: The service socket for FaultFuzz-ctrl.
- **aflPort**: The port used by FaultFuzz-inst for network communication with FaultFuzz-ctrl.

- **observerHome**: The root data folder uses in observer. if **observerHome** is set, FaultFuzz will use **observerHome/fav-rst** as the default value of **recordPath**, **observerHome/fuzzcov** as the default value of **covPath**, and **observerHome/CacheFolder** as the default value of **cacheDir**. 
- **recordPath**: The location where FaultFuzz records intermediate data. It will override the default value of **observerHome/fav-rst**.
- **covPath**: The location where FaultFuzz stores coverage statistics data. It will override the default value of **observerHome/covPath**.
- **cacheDir**: The location for recorded instrumentation cache data. Recording this cache data can improve FaultFuzz's performance and provide more debugging information. It will override the default value of **observerHome/CacheFolder**.

- **dataPaths**: The location of the target system's data. It is important to use this option to filter the I/O operations unrelated of target system.

- **mapSize**: The total memory size used by FaultFuzz for block coverage statistics.
- **workSize**: The memory size used by FaultFuzz for coverage statistics of each code block.

- **covIncludes**: FaultFuzz only records the coverage information of classes specified by this configuration item's prefix.

- **aflAllow**: FaultFuzz only instruments the classes specified by prefixes in this file.
- **aflDeny**: FaultFuzz does not instrument the classes specified by prefixes in this file.

- **useMsgid**: Assigns MessageId to the messages sent in the system, enabling FaultFuzz to match the messages sent and received by optimizing the testing process.
- **jdkFile**: Uses FaultFuzz's built-in JDK-level file I/O instrumentation mechanism, which instruments file read and write operations in the JDK.
- **jdkMsg**: Uses FaultFuzz's built-in JDK-level message I/O instrumentation mechanism, which instruments message read and write operations in the JDK.
- **forZk**: Uses FaultFuzz's built-in Zookeeper application-level I/O instrumentation mechanism for instrumenting Zookeeper.
- **zkApi**: Uses FaultFuzz's built-in Zookeeper application-level I/O instrumentation mechanism for instrumenting the API interfaces exposed by Zookeeper to the upper-level system.
- **forHdfs**: Uses FaultFuzz's built-in HDFS application-level I/O instrumentation mechanism for instrumenting HDFS.
- **hdfsApi**: Uses FaultFuzz's built-in HDFS application-level I/O instrumentation mechanism for instrumenting the API interfaces exposed by HDFS to the upper-level system.
- **forHBase**: Uses FaultFuzz's built-in HBase application-level I/O instrumentation mechanism for instrumenting HBase.
