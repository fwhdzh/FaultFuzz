# FaultFuzz

## What is FaultFuzz
FaultFuzz is a tool designed for fault injection testing for distributed systems. Leveraging Fuzzing techniques, FaultFuzz enables systematic fault injection testing at the I/O point granularity of the target distributed system.

## Paper
For more information on the design of FaultFuzz, please refer to the following papers:

- Coverage Guided Fault Injection for Cloud Systems
- FaultFuzz: Coverage Guided Fault Injection for Distributed Systems (Under Submission)

## Project structrue
FaultFuzz comprises several sub-projects, including:

- A Java command-line program (FaultFuzz-inst) for collecting runtime information of the target system
- A Java command-line program (FaultFuzz-ctrl) for controlling the testing process
- A Spring Boot server program (FaultFuzz-backend) providing a visual interface for the control program, and an AppSmith web application (FaultFuzz-FrontEnd)
- A set of test cases (FaultFuzz-workloads) used in our previous paper experiments.

## How dose FaultFuzz work
After the preparation of the operating environment, FaultFuzz can be divided into four main parts.

Firstly, FaultFuzz runs the target system once without injecting any faults. During this run, FaultFuzz collects all the I/O points the system reaches. After this run, FaultFuzz obtains an I/O point sequence that reflects the system's operational history. We regard this sequence as the first fault sequence.

Next, FaultFuzz mutates this I/O sequence. Specifically, it selects an I/O point and injects a new fault into it. This results in a fault sequence, representing the system's traversal of all the I/O points before reaching the selected one and encountering the injected fault upon arrival. FaultFuzz's mutation is systematic, meaning it generates all possible fault sequences at this step.

Subsequently, FaultFuzz employs its built-in strategy to select the next error sequence that can most easily test the system's new behaviors. The specific selection strategy can be found in the paper.

Finally, FaultFuzz conducts the next round of error sequence testing based on the selected error sequence. It controls the system's behavior, injecting faults when the system reaches the designated I/O points for fault injection.

During this fault-injected testing, FaultFuzz observes new system behaviors, thus initiating the cycle of mutation-selection-testing again. FaultFuzz's testing continues until it cannot generate any untested error sequences. Users can also specify test conditions such as the total duration of testing.

<!-- New feature -->

<!-- How to Use? -->
## How to use?
Users need to follow the steps below to use FaultFuzz:

### Step 1: Install FaultFuzz

```
git clone https://github.com/fwhdzh/FaultFuzz.git
cd faultfuzz
mvn clean install
```

### Step 2: Launch the FaultFuzz server.
```
cd faultfuzz-backend
mvn spring-boot:run
```

### Step 3: (Optional) Preparing an Instrumented JDK

FaultFuzz includes built-in mechanisms for for JDK level file I/O and message I/O instrumentation. Users can enable this functionality through simple configuration. If users want to use the instrumentation, they need to instrument jdk using the following instructions.

```
java -jar FaultFuzz-inst.jar -forJava <jdk_path> <instrument_jdk_path>

rm <instrument_jdk_path>/jre/lib/jce.jar
cp <our_provided_jre>/jre/lib/jce.jar <instrument_jdk_path>/jre/lib
rm -r <instrument_jdk_path>/jre/lib/security/policy/*
cp -r <our_provided_jre>/jre/lib/security/policy/* <instrument_jdk_path>/jre/lib/security/policy
chmod +x <instrument_jdk_path>/bin/*
```

(Consider providing an already instrumented JDK rather than having users instrument it themselves)

Afterwards, they can set `$JAVA_HOME` to `<instrumented_jdk_path>`, enabling the subsequent `java` commands to be interpreted as `<instrumented_jdk_path>/bin/java`.

As such, in the configuration, users can set `jdkFile=true` and `jdkMsg=true` to apply the instrumentation on file I/O and message I/O at the JDK level. 

When using this, it is important to ensure that the process of the program under test is executed by `<instrumented_jdk_path>/bin/java`, rather than a regular `java` command.

### Step 4:  Preparation of user-defined I/O points.

FaultFuzz allows users to annotate the I/O points within the target system for testing purposes. It offers both annotation-based marking and API-based marking functionalities.

To annotate the I/O points, users first need to add a dependency on FaultFuzz-inst in their project. If the target system is based on Maven structure, they can add the following to the pom.xml file:

```
<dependencies>
    <dependency>
        <groupId>edu.iscas.tcse</groupId>
        <artifactId>FaultFuzz-inst</artifactId>
        <version>0.0.5-SNAPSHOT</version>
    </dependency>
</dependencies>
```

For other systems, users can reference the dependency by directly specifying the FaultFuzz-inst.jar path or through other appropriate methods.

If a user intends to designate a specific location in the system as an I/O point, they can use the API WaitToExec.triggerAndRecordFaultPoint(String path); for annotation. Here, the 'path' parameter is additional information used by FaultFuzz to identify the I/O point, and it is generally set to the file path or message content.

Here's an example:

```
String filePath = "/data/fengwenhan/data/faultfuzz_bt/1.txt";
File file = new File(filePath);
if (!file.exists()) {
    file.createNewFile();
}
FileOutputStream outputStream = new FileOutputStream(file);
WaitToExec.triggerAndRecordFaultPoint(filePath);
outputStream.write(54); // ASCII for 6
```

FaultFuzz will consider the 6th line in the example above as an I/O point.

If a user wishes to designate a specific function in the system as an I/O function (all function calls within the system will be identified as I/O points), they can add the @Inject annotation to that function.

Here's an example:

```
public class SocketCilent {
    @Inject
    public void startConnection(String ip, int port) throws IOException {
        System.out.println("Client begin connect");
        clientSocket = new Socket(ip, port);
        ......
    }

    public static void main(String[] args) throws IOException {
        SocketCilent client = new SocketCilent();
        client.startConnection("127.0.0.1", 12001);
    }
}

```

In this example, since startConnection has been annotated with @Inject, the call to startConnection in the second line of the main function will be treated as an I/O point.

Once the user has completed all the annotations, they need to use the following command to extract the relevant information into <info_file>:

```
java -cp FaultFuzz-inst.jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier <target_project_path> <info_file>
```

In the subsequent testing process, users can inform FaultFuzz to use the information stored in <info_file> through configuration.

### Step 5: Creating the FaultFuzz configuration file.

Before running FaultFuzz, users need to configure it. We provide a user interface (UI) to facilitate the generation of the relevant configuration file.

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


Users can view the relevant configurations in our visual interface and generate them to a specified location by clicking a button.

FaultFuzz will generate two files for you, named ctrl.properties and fav_env.sh.
Among them, fav_env.sh provides two Linux environment variables.

```
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,useMsgid=false,jdkMsg=false"

export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,forZk=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPath=/home/gaoyu/zk363-fav-rst/,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/home/gaoyu/CacheFolder,controllerSocket=172.30.0.1:12090,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/zookeeper,aflAllow=/home/gaoyu/evaluation/zk-3.8.1/allowlist,aflDeny=/home/gaoyu/evaluation/zk-3.8.1/denylist,aflPort=12081,execMode=FaultFuzz"
```

Where FAV_OPTS is an environment variable generated based on the user-defined configuration. PHOS_OPTS, on the other hand, retains additional information introduced by FaultFuzz on top of FAV_OPTS, but without control. PHOS_OPTS is particularly useful in distributed systems where multiple processes interact. For instance, in the case of Zookeeper, we may want the server processes to be controlled by FaultFuzz while the client processes remain uncontrolled.

### Step 6:  Deploying the cluster.

Users need to independently deploy their own testing clusters and add the relevant configuration items to the startup files of the clusters.

Generally, users only need to add the corresponding parameters to the startup files of the original cluster nodes.


For instance, in Zookeeper, the method involves modifying the zkEnv.sh file. Add FAV_OPTS and PHOS_OPTS to the SERVER_JVMFLAGS and CLIENT_JVMFLAGS used during Zookeeper startup.

```
export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS $FAV_OPTS $TIME_OPTS"

export CLIENT_JVMFLAGS="-Xmx${ZK_CLIENT_HEAP}m $CLIENT_JVMFLAGS $PHOS_OPTS $TIME_OPTS"

```

### Step 7:  Start the testing and observe the results.
Users can initiate the testing by clicking on the test button on the interface. FaultFuzz will automatically display the test results during the testing process.