# FaultFuzz

## What is FaultFuzz
FaultFuzz is a tool designed for fault injection testing for distributed systems. Leveraging Fuzzing techniques, FaultFuzz enables systematic fault injection testing at the I/O point granularity of the target distributed system.

<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-overview.png) -->

<div style="text-align:center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-overview.png" style="width:60%">
</div>

## Quick start
We have prepared a Zookeeper cluster based on Docker and completed most of the
configuration and preparation. Users can
quickly experience FaultFuzz through this cluster.

To quickly experience FaultFuzz with the Zookeeper cluster, please see the document [Quick-start](/doc/Quick-start.md)

## Paper
For more information on the design of FaultFuzz, please refer to the following papers:

- Coverage Guided Fault Injection for Cloud Systems (ICSE 2023)
- FaultFuzz: Coverage Guided Fault Injection for Distributed Systems (Under Submission)

## New features
We extend our previous work CrashFuzz in several aspects, and sup-
port more features in FaultFuzz. 

First, FaultFuzz can support more fault types, i.e., network dis connection and reconnection. and users can flexibly specify the
concerned fault types. 

Second, FaultFuzz can support multiple work-
loads to drive the test, which can facilitate fault scenario space
exploration and bug discovery. 

Third, FaultFuzz can also support
manual annotation of the target distributed system to indicate
which application-level I/O points are interesting and should be
tracked by FaultFuzz. In this way, FaultFuzz can be easily applied
to a new distributed system. 

Finally, FaultFuzz can control more
non-determinism among the collected events during system test-
ing. Therefore, we can more faithfully reproduce fault sequences
during system testing. 

To get more detailed information about new features, please see our paper and the document [New features](/doc/NEWFEATURE.md).

## Project structrue
FaultFuzz comprises several sub-projects, including:

- A Java command-line program (FaultFuzz-inst) for collecting runtime information of the target system (Observer)
- A Java command-line program (FaultFuzz-ctrl) for controlling the testing process (Controller)
- A Spring Boot server program (FaultFuzz-backend) providing a visual interface for the control program, and an AppSmith web application (FaultFuzz-FrontEnd)
- A set of test cases (FaultFuzz-workloads) used in our previous paper experiments.

The FaultFuzz-FrontEnd is stored as a standlone project. We have deployed it on
[AppSmith cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master).
Users can also deploy the frontend by themselves with our [published frontend source codes](https://github.com/fwhdzh/FaultFuzz-FrontEnd).


## How dose FaultFuzz work
After the preparation of the operating environment, FaultFuzz can be divided
into four main parts.

Firstly, FaultFuzz runs the target system once without injecting any faults.
During this run, FaultFuzz collects all the I/O points the system reaches. After
this run, FaultFuzz obtains an I/O point sequence that reflects the system's
operational history. We regard this sequence as the first fault sequence.

Next, FaultFuzz mutates this I/O sequence. Specifically, it selects an I/O point
and injects a new fault into it. This results in a fault sequence, representing
the system's traversal of all the I/O points before reaching the selected one
and encountering the injected fault upon arrival. FaultFuzz's mutation is
systematic, meaning it generates all possible fault sequences at this step.

Subsequently, FaultFuzz employs its built-in strategy to select the next error
sequence that can most easily test the system's new behaviors. The specific
selection strategy can be found in the paper.

Finally, FaultFuzz conducts the next round of error sequence testing based on
the selected error sequence. It controls the system's behavior, injecting faults
when the system reaches the designated I/O points for fault injection.

During this fault-injected testing, FaultFuzz observes new system behaviors,
thus initiating the cycle of mutation-selection-testing again. FaultFuzz's
testing continues until it cannot generate any untested error sequences. Users
can also specify test conditions such as the total duration of testing.

<!-- New feature -->

<!-- How to Use? -->
## How to use?
Users need to follow the steps below to use FaultFuzz:

### Step 1: Install FaultFuzz and start FaultFuzz server.


```
git clone https://github.com/fwhdzh/FaultFuzz.git

cd faultfuzz
mvn clean install

cd faultfuzz-backend
mvn spring-boot:run
```

Then the user can access the test server through the frontend of FaultFuzz.
After inputting the address of the test server, the user can click the "check
connection" button to confirm that the backend has been started and the frontend
can correctly connect to the backend. 

We have provided a visual frontend as a website on [AppSmith
cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master).
Users can also deploy the frontend website on their own computer with our
published frontend source codes.


### Step 2:  (Optional) Preparation of user-defined I/O points.

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

Users need to set "useInjectAnnotation=true" in the FaultFuzz configuration file for SUT to tell FaultFuzz to handle the annotations.

<!-- Once the user has completed all the annotations, they need to use the following command to extract the relevant information into <info_file>:

```
java -cp FaultFuzz-inst.jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier <target_project_path> <info_file>
```

In the subsequent testing process, users can inform FaultFuzz to use the information stored in <info_file> through configuration. -->

### Step 3: Configure FaultFuzz and SUT.

We provide a “Configuration” web page (see Figure 3- 2 ) for users to specify
the configurations used to test a target distributed system. The configurations
can be divided into four categories, i.e., “Workloads & bug checker”, “Faults &
fault injection points”, “Observer” and “Test controller”.

<!-- The “Workloads & bug checker” panel allows users to specify the string paths of
scripts used for driving SUT and confirming bugs, e.g., the script for
requesting SUT, the script for resetting SUT to an initial state, and the script
for detecting system failure symptoms.

The “Faults & fault injection points” panel allows users to customize concerned
fault sequences, such as concerned fault types (i.e., node crashes/reboots and
network disconnection/reconnection), concerned fault injection points (e.g.,
JRE-level disk I/O points and App-level network I/O points), the maximum number
of faults in a fault sequence, etc.

The “Observer” panel allows users to specify the information used for
instrumenting the target system, e.g., the classes to instrument, the root path
for storing runtime information, the port used by each node in SUT to
communicate with FaultFuzz’s test controller and so on.

The “Test controller” panel allows users to specify the information used by
FaultFuzz’s test controller, e.g., the testing time budget, the path for storing
the test results, the IP address and port of the test controller, the IP
addresses of the nodes in SUT, etc. -->

After entering the above configuration information, users can click the
``Generate configuration files'' button to generate and download two
configuration files, named FaultFuzz-backend-configuration.properties and
FaultFuzz-SUT-configuration.sh.

The file FaultFuzz-backend-configuration.properties should be uploaded to the
backend of FaultFuzz. The FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar will use
FaultFuzz-backend-configuration.properties as an input argument.

The FaultFuzz-SUT-configuration.sh should be copied to each node in SUT. In our
Zookeeper cluster, we have prepared the environment already. So the only thing
the users need to do is copy FaultFuzz-SUT-configuration.sh to
`/SUT-configuration` folder of each docker container.

The FaultFuzz-SUT-configuration.sh. is used for the observers in the SUT. Generally, this file provides two Linux environment variables.

```
export PHOS_OPTS="-Xbootclasspath/a:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false"

export FAV_OPTS="-Xbootclasspath/a:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/SUT-configuration/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,forZk=true,jdkFile=true,observerHome=/observer,dataPaths=/zookeeper-3.6.3/zkData/version-2,controllerSocket=172.30.0.1:12090,covIncludes=org/apache/zookeeper,aflAllow=/SUT-configuration/allowlist,aflDeny=/SUT-configuration/denylist,aflPort=12081"
```

Where FAV_OPTS is an environment variable generated based on the user-defined configuration. PHOS_OPTS, on the other hand, retains additional information introduced by FaultFuzz on top of FAV_OPTS, but without control. PHOS_OPTS is particularly useful in distributed systems where multiple processes interact. For instance, in the case of Zookeeper, we may want the server processes to be controlled by FaultFuzz while the client processes remain uncontrolled.

Users need to independently the FaultFuzz-SUT-configuration.sh file items to the launching script of their SUT. To use our built-in I/O point information, they also need to make the target system run in an instrumented JRE in the launching script. We have provided the instrumented JRE in our artifact package. Users can also instrument a JRE by themselves with the command `java -jar FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava <jre\_path> <output\_path>`.

For instance, in Zookeeper, the method involves modifying the zkEnv.sh file. Add FAV_OPTS and PHOS_OPTS to the SERVER_JVMFLAGS and CLIENT_JVMFLAGS used during Zookeeper startup.

```

JAVA="<instrumented_JRE_HOME>/bin/java"

. <your_configuration_folder>/FaultFuzz-SUT-configuration.sh

export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS $FAV_OPTS $TIME_OPTS"
export CLIENT_JVMFLAGS="-Xmx${ZK_CLIENT_HEAP}m $CLIENT_JVMFLAGS $PHOS_OPTS $TIME_OPTS"

```

If you are interesting in the meaning of each configuration item in these configuration files, please refer to our [configuration documentation](/doc/Configuration.md). Note that the configurations in the document are more low-level and slightly different from the configuration items provided in our configuration interface. In the configuration interface, we have simplified the configuration information to make it more human-understandable.

### Step 4: Start the testing and observe the results.

After finishing configuration, users can go to the `Test and result`
page, enter the path of FaultFuzz test controller jar file and the path of the configuration file. When a user clicks the `Start
test` button, FaultFuzz will automatically perform fault injection testing for SUT. Users can also pause, resume or stop the test by clicking the corresponding buttons.

FaultFuzz displays quantitative statistics of the runtime test results at the bottom of the web page, including the elapsed testing time, the total number of detected bugs, the total number of tested fault sequences, the
total number of covered basic code blocks and so on.
If the user wants to further observe one specific bug, she can check the detailed bug reports. The user can also try to replay a bug by entering the file path of the fault sequence that triggers the bug and clicking the
`Start replay` button.