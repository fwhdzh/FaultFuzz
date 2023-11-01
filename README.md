# FaultFuzz

## What is FaultFuzz
FaultFuzz is a tool designed for fault injection testing for distributed systems. Leveraging Fuzzing techniques, FaultFuzz enables systematic fault injection testing at the I/O point granularity of the target distributed system.

<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-overview.png) -->

<p align="center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-overview.png" style="width:60%">
</p>

## Quick start
We have prepared a Zookeeper cluster based on Docker and completed most of the
configuration and preparation tasks. Users can
quickly experience FaultFuzz through this cluster.

To quickly experience FaultFuzz with the Zookeeper cluster, please see the document [Quick-start](/doc/Quick-start.md).

## Paper
For more information on the design of FaultFuzz, please refer to the following papers:

- Coverage Guided Fault Injection for Cloud Systems (ICSE 2023)
- FaultFuzz: Coverage Guided Fault Injection for Distributed Systems (Under Submission)

## New features
We extend our previous work CrashFuzz in several aspects, and support more features in FaultFuzz. 

First, FaultFuzz can support more fault types, i.e., network disconnection and
reconnection. and users can flexibly specify the concerned fault types. 

Second, FaultFuzz can support multiple workloads to drive the test, which can
facilitate fault scenario space exploration and bug discovery. 

Third, FaultFuzz can also support manual annotation of the target distributed
system to indicate which application-level I/O points are interesting and should
be tracked by FaultFuzz. In this way, FaultFuzz can be easily applied to a new
distributed system. 

Finally, FaultFuzz can control more non-determinism among the collected events
during system testing. Therefore, we can more faithfully reproduce fault
sequences during system testing. 

To get more detailed information about new features, please see our paper and the document [New features](/doc/NEWFEATURE.md).

## Project structrue
FaultFuzz comprises several sub-projects, including:

- A Java command-line program (FaultFuzz-inst) for collecting runtime information of the target system (Observer)
- A Java command-line program (FaultFuzz-ctrl) for controlling the testing process (Controller)
- A Spring Boot server program (FaultFuzz-backend) and an AppSmith web application (FaultFuzz-FrontEnd), which provide a visual interface for the control program.
- A set of test workloads (FaultFuzz-workloads) used in our previous paper experiments.

The FaultFuzz-FrontEnd is maintained as a standalone project, and we have deployed it on
[AppSmith cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master).
Users also have the option to independently deploy the frontend using our [published frontend source codes](https://github.com/fwhdzh/FaultFuzz-FrontEnd).


## How dose FaultFuzz work
After the preparation of the test environment, FFaultFuzz operates in four primary stages. 

Firstly, FaultFuzz runs the target system once without injecting any faults.
During this run, it captures all the system's I/O points. The resulting sequence
of I/O points reflects the system's operational history, which is considered the
initial fault sequence.

Next, FaultFuzz mutates this I/O sequence. Specifically, it selects an I/O point
and injects a new fault into it. This process generates fault sequences that
depict the system's traversal of I/O points before encountering the injected
fault at the selected point. FaultFuzz's mutation process is systematic, which
means it produces all potential fault sequences at this stage.

Subsequently, FaultFuzz employs its built-in strategies to select the next fault
sequence that can most effectively test the system's new behaviors. The
specifics of these selection strategies can be found in the paper.

Finally, FaultFuzz proceeds to conduct the next round of fault sequence testing
based on the chosen fault sequence. It controls the system's behavior, injecting
faults when the system reaches the I/O points choosen for faults.

During this fault-injected testing phase, FaultFuzz monitors the system for new
behaviors, and subsequently repeats the processes of fault sequence generation,
mutation, selection, and testing. The testing process of FaultFuzz persists
until it has exhausted all untested fault sequences. Users can also specify the
total testing time to give a stop condition of the testing process.

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

User can update `faultfuzz-backend/src/main/resources/application.properties` to specify the port that the server runs on.

Then the user can access the test server via the FaultFuzz frontend.
After entering the test server's address, the user can click the "check
connection" button to confirm that the backend is running and the frontend
can uccessfully establishes a connection with the backend. 

We have provided deploy FaultFuzz frontend as a website on [AppSmith
cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master).
Users can also deploy the frontend website on their own computer using our accessible [frontend source code](https://github.com/fwhdzh/FaultFuzz-FrontEnd).


### Step 2:  (Optional) Preparation of user-defined I/O points.

FaultFuzz allows users to annotate the I/O points within the target system for
testing purposes, providing both annotation-based marking and API-based marking
functionalities.

To annotate the I/O points, users first need to integrate a dependency on
FaultFuzz-inst into their project. If the target system is based on Maven
structure, they can add the following dependency to the pom.xml file:

```
<dependencies>
    <dependency>
        <groupId>edu.iscas.tcse</groupId>
        <artifactId>FaultFuzz-inst</artifactId>
        <version>0.0.5-SNAPSHOT</version>
    </dependency>
</dependencies>
```

For other systems, users can reference the dependency by directly specifying the
FaultFuzz-inst.jar path or by employing other suitable methods.

If a user intends to specify a line within the system as an I/O point, they can
utilize the API `WaitToExec.triggerAndRecordFaultPoint(String path)` for
annotation. In this context, the `path` parameter serves as additional
information employed by FaultFuzz to identify the I/O point, typically set to
the file path or message content.

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

If a user intends to specify a function in the system as an I/O function (with all function calls within the system identified as I/O points), they can simply add the @Inject annotation to the desired function.

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

In this scenario, since the function `startConnection` has been annotated with @Inject, the call to startConnection on the second line of the main function will be recognized as an I/O point.

To enable FaultFuzz to process these annotations, users must either select `App-level network I/O points` in the configuration page of the FaultFuzz frontEnd website or set `useInjectAnnotation=true` in the FaultFuzz configuration file for the system under test (SUT).

<!-- Once the user has completed all the annotations, they need to use the following command to extract the relevant information into <info_file>:

```
java -cp FaultFuzz-inst.jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier <target_project_path> <info_file>
```

In the subsequent testing process, users can inform FaultFuzz to use the information stored in <info_file> through configuration. -->

### Step 3: Configure FaultFuzz and SUT.

We provide a “Configuration” web page in FaultFuzz frontend for users to specify
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
`Generate configuration files` button to generate and download two
configuration files, named FaultFuzz-backend-configuration.properties and
FaultFuzz-SUT-configuration.sh. If you are interesting in the meaning of each configuration item in these
configuration files, please refer to our [configuration documentation](/doc/Configuration.md).

The file FaultFuzz-backend-configuration.properties should be uploaded to the
backend of FaultFuzz. The FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar will use
FaultFuzz-backend-configuration.properties as an input argument. 

The FaultFuzz-SUT-configuration.sh should be copied to each node in SUT.
Besides, Users need to intergrate the FaultFuzz-SUT-configuration.sh file items
to the launching script of their SUT. If they want to use JRE-level I/O points,
they also need to make the target system run in an instrumented JRE in the
launching script. For instance, in Zookeeper, users can modify the part of 
zkEnv.sh file as bellow (`FAV_OPTS` and `PHOS_OPTS` are environment variables
provided in FaultFuzz-SUT-configuration.sh).

```

JAVA="<instrumented_JRE_HOME>/bin/java"

. <your_configuration_folder>/FaultFuzz-SUT-configuration.sh

export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS $FAV_OPTS $TIME_OPTS"
export CLIENT_JVMFLAGS="-Xmx${ZK_CLIENT_HEAP}m $CLIENT_JVMFLAGS $PHOS_OPTS $TIME_OPTS"

```

We have provided the instrumented JRE in our artifact package. Users can
also instrument a JRE by themselves with the command `java -jar FaultFuzz-inst-0.0.5-SNAPSHOT.jar -forJava <jre\_path> <output\_path>`.


### Step 4: Start the testing and observe the results.

After finishing configuration, users can go to the `Test and result` page, enter
the path of FaultFuzz test controller jar file and the path of the configuration
file. When a user clicks the `Start test` button, FaultFuzz will automatically
perform fault injection testing for SUT. Users can also pause, resume or stop
the test by clicking the corresponding buttons.

FaultFuzz displays quantitative statistics of the runtime test results at the
bottom of the web page, including the elapsed testing time, the total number of
detected bugs, the total number of tested fault sequences, the total number of
covered basic code blocks and so on. If the user wants to further observe one
specific bug, she can check the detailed bug reports. The user can also try to
replay a bug by entering the file path of the fault sequence that triggers the
bug and clicking the `Start replay` button.