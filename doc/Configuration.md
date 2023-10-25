The configuration options supported by FaultFuzz-ctrl include:

* WORKLOAD: The string path for the script used for running a workload.
* CHECKER: The string path for the script used for checking failure symptoms.

* FAULT_TYPE: The fault types to be injected to the systems. E.g., [CRASH,NETWORK_DISCONNECTIOn]
* CRASH: The string path for the script used for crash a node in the
  target system according to the ip address.
* REBOOT: The string path for the script used for reboot a crash
  node according to the ip address.
* NETWORK_DISCONNECTION: The string path for the script used for disconnect 
  netword between two nodes according to the ip addresses.
* NETWORK_RECONNECTION: The string path for the script used for reconnect 
  netword between two nodes according to the ip addresses.

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

* MONITOR: The string path for the script used for collecting runtime
  information from the target cluster.
* PRETREATMENT: The string path for the script used for prepare a
  clean environment for the target cluster before every fault
  injection test, e.g., remove stale data from last test.

* ROOT_DIR: The string path for the directory used for storing test
  outputs.
* CUR_FAULT_FILE: The string path for the file used for storing
  current fault sequence under test.

* TEST_TIME: To specify the test time.
* HANG_TIMEOUT: To specify the timeout period that used for confirming a
  hang bug.
* DETERMINE_WAIT_TIME: To specify the timeout period that used for 
  giving up to wait next I/O operation.


The configuration options supported by FaultFuzz-inst include:

- **useFaultFuzz:** Controls the usage of FaultFuzz for the program or process.

- **controllerSocket:** The service socket for FaultFuzz-ctrl.
- **aflPort:** The port used by FaultFuzz-inst for network communication with FaultFuzz-ctrl.

- **recordPath:** The location where FaultFuzz records intermediate data.
- **dataPaths:** The location of the target system's data. (Seems to be only useful during JDK instrumentation.)
- **cacheDir:** The location for recorded instrumentation cache data. Recording this cache data can improve FaultFuzz's performance and provide more debugging information.

- **mapSize:** The total memory size used by FaultFuzz for block coverage statistics.
- **workSize:** The memory size used by FaultFuzz for coverage statistics of each code block.
- **covPath:** The location where FaultFuzz stores coverage statistics data.
- **covIncludes:** FaultFuzz only records the coverage information of classes specified by this configuration item's prefix.

- **aflAllow:** FaultFuzz only instruments the classes specified by prefixes in this file.
- **aflDeny:** FaultFuzz does not instrument the classes specified by prefixes in this file.

- **useMsgid:** Assigns MessageId to the messages sent in the system, enabling FaultFuzz to match the messages sent and received by optimizing the testing process.
- **jdkFile:** Uses FaultFuzz's built-in JDK-level file I/O instrumentation mechanism, which instruments file read and write operations in the JDK.
- **jdkMsg:** Uses FaultFuzz's built-in JDK-level message I/O instrumentation mechanism, which instruments message read and write operations in the JDK.
- **forZk:** Uses FaultFuzz's built-in Zookeeper application-level I/O instrumentation mechanism for instrumenting Zookeeper.
- **zkApi:** Uses FaultFuzz's built-in Zookeeper application-level I/O instrumentation mechanism for instrumenting the API interfaces exposed by Zookeeper to the upper-level system.
- **forHdfs:** Uses FaultFuzz's built-in HDFS application-level I/O instrumentation mechanism for instrumenting HDFS.
- **hdfsApi:** Uses FaultFuzz's built-in HDFS application-level I/O instrumentation mechanism for instrumenting the API interfaces exposed by HDFS to the upper-level system.
- **forHBase:** Uses FaultFuzz's built-in HBase application-level I/O instrumentation mechanism for instrumenting HBase.
