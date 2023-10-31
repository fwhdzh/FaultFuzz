Compared to CrashFuzz, FaultFuzz has made the following improvements:
## Non-determinism control
CrashFuzz only controls system behavior based on the frequency of occurrence of I/O points. Consider the following example.

| A | B | 
| :-------: | :-------: | 
| write(B, 1) |  |
| | read(A, 1) |
| | write(disk, log) |
| write(B, 2) |  |
|  | read(A, 2) |

where write(disk, log) represents the operation of the system periodically writing logs to the disk.

Suppose we do not introduce any non-deterministic control. When we let the system run again, it might become:

| A | B | 
| :-------: | :-------: | 
| write(B, 1) |  |
| | write(disk, log) |
| | read(A, 1) |
| write(B, 2) |  |
|  | read(A, 2) |



Obviously, the two examples above represent two different system test scenarios. If we inject a crash fault into node B at the `read(A, 1)` location for both, we are likely to observe different system recovery behaviors.

The task that CrashFuzz can accomplish is "to inject a fault into node B when `read(A, 1)` occurs for the first time." Since CrashFuzz does not conduct deterministic control over the system under test, it cannot guarantee whether the system will run as scenario 1 or as scenario 2 during the test. In other words, if CrashFuzz wants to test scenario 1, it can only hope that the system randomly exhibits scenario 1.

In FaultFuzz, we have implemented deterministic control functionality. If FaultFuzz wants to test scenario 1, it will control the system under test to wait for the controller's response at every I/O point to determine whether to continue execution or wait. Consequently, FaultFuzz can ensure that the `write(disk, log)` operation of node B will definitely be executed after `read(A, 1)`.

Note that FaultFuzz does not guarantee a perfect deterministic control to handle all non-determinism. Non-deterministic control in distributed systems has always been a challenging problem. FaultFuzz provides the DETERMINE_WAIT_TIME parameter, indicating the maximum waiting time for non-deterministic control by FaultFuzz. When an expected I/O point does not occur within the maximum waiting time, FaultFuzz will abandon non-deterministic control for the current test.

## New fault type support: network disconnection and reconnction

In FaultFuzz, we introduce two new fault types, network disconnection, and reconnection.

Let's assume we have two nodes, A and B. In FaultFuzz, we define network disconnection as the inability of all messages from node A to node B to reach their destination correctly. Network reconnection, on the other hand, restores the network from node A to node B, allowing subsequent messages from node A to node B to be delivered correctly.

It's important to note that network disconnection is unidirectional, meaning FaultFuzz does not intercept messages from node B to node A. However, considering that most distributed systems use the TCP protocol for their network communication, and the TCP protocol requires an acknowledgment (ACK) for every message sent, this leads to a situation where in most system implementations, network disconnection will prevent any communication between the two nodes.

If the user specifies network disconnection and reconnection in the fault types in the configuration, FaultFuzz will incorporate the injection of network disconnection and reconnection fault sequences when generating fault sequences. FaultFuzz also controls fault sequence generation through built-in constraints, such as "inject reconnection only on a network path that has already been disconnected."

The implementation of network disconnection and reconnection in FaultFuzz is based on Docker, and we plan to implement network faults for other systems in the future.

## Manual annotation to test users' own distributed system

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

## Multiple workload support

A workload is a series of cluster startup operations, user operations and admin operations.

CrashFuzz can only support one workload in its fault scenarios explaination, which limits its ability on bug discovery. 

In FaultFuzz, you can specify multiple workloads in the configuration files like: 
```
WORKLOAD={/zookeeper/workload-1.sh,/zookeeper/workload-2.sh}
```

In the testing process, FaultFuzz will first run all the workloads separately on SUT without injecting any faults. Then, FaultFuzz will put all fault sequences from differents workloads into a same pool to find new interesting fault scenarios.

## User interface

Last but not the least, we provided a visual frontend as a website.
With this website, users can specify the testing configuration, running the test and see the quantitative statistics of the runtime test results. 

We have deployed our website on [AppSmith
cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master). We also provide the source codes of our website on [Github](https://github.com/fwhdzh/FaultFuzz-FrontEnd) so that users can deploy the website by themselves if they want.