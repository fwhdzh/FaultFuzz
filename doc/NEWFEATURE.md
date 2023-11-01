Compared to CrashFuzz, FaultFuzz has made the following improvements:

## Non-determinism control

CrashFuzz only controls system behavior based on the frequency of I/O points occurrence. Consider the following example.

| A | B | 
| :-------: | :-------: | 
| write(B, 1) |  |
| | read(A, 1) |
| | write(disk, log) |
| write(B, 2) |  |
|  | read(A, 2) |

where `write(disk, log)` represents the system operation of periodically writing logs to the disk.

Suppose we do not implement any non-deterministic control. When we let the system run again, it might become:

| A | B | 
| :-------: | :-------: | 
| write(B, 1) |  |
| | write(disk, log) |
| | read(A, 1) |
| write(B, 2) |  |
|  | read(A, 2) |

Obviously, the two examples above represent two different test scenarios. If we inject a crash fault into node B at the `read(A, 1)` location for both cases, we are likely to observe different system recovery behaviors.

The task that CrashFuzz can accomplish is "inject a fault into node B when `read(A, 1)` occurs for the first time." Since CrashFuzz lacks deterministic control over the system under test, it cannot guarantee whether the system will run as the first scenario or the second scenario during the test. In another word, if CrashFuzz wants to test the first scenario 1, it can only rely on the system randomly exhibiting the first scenario.

In contrast, FaultFuzz incorporates deterministic control functionality. If FaultFuzz wants to test the first scenario, it orchestrates the system under test to await the controller's response at each I/O point, determining whether to proceed or wai.. Consequently, FaultFuzz can ensure that the `write(disk, log)` operation of node B always be executed after `read(A, 1)`.

However, it's important to note that FaultFuzz does not promise flawless deterministic control of all non-determinism. Non-deterministic control in distributed systems has always posed a challenging problem. To handle this, FaultFuzz provides the DETERMINE_WAIT_TIME parameter, specifying the maximum waiting time for non-deterministic control. If an expected I/O point fails to occur within this maximum waiting time, FaultFuzz will abandon non-deterministic control for the ongoing test.

## New fault type support: network disconnection and reconnction

In FaultFuzz, we introduce two new fault types: network disconnection and reconnection.

Let's consider two nodes, A and B. Network disconnection entails the failure of all messages from node A to node B to reach their intended destination. On the contrary, network reconnection re-establishes the network from node A to node B, ensuring the correct delivery of subsequent messages from node A to node B. FaultFuzz also guarantees some constraints for injecting these two types of faults, e.g., injecting network reconnection only on a network path that has already been disconnected.

Notably, network disconnection in FaultFuzz operates unidirectionally, without intercepting messages from node B to node A. However, since most distributed systems utilize the TCP protocol for their network communication, which requires an acknowledgment (ACK) for every sent message, network disconnection typically prevents any communication between the two nodes since it can intercept the ACKs in these system implementations.

The implementation of network disconnection and reconnection in FaultFuzz relies on Docker, and we plan to implement network faults for other systems in the future.

## Manual annotation to test users' own distributed systems

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

## Multiple workload support

A workload is a series of cluster startup operations, user operations, and admin operations.

CrashFuzz is limited to support only one workload in its fault scenario explanation, constraining its capability for bug discovery.

In FaultFuzz, you can specify multiple workloads in the configuration files as follows:

```
WORKLOAD={/zookeeper/workload-1.sh,/zookeeper/workload-2.sh}
```

During testing, FaultFuzz initially executes all the workloads separately on the System Under Test (SUT) without injecting any faults. Subsequently, it consolidates all fault sequences generated from different workloads into a unified pool to select interesting fault scenarios in a global view.

## User interface

Last but not least, we have provided a visual frontend in the form of a website. This frontend allows users to define testing configurations, run tests, and observe quantitative statistics of the runtime test results

We have deployed our website on [AppSmith
cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master). We also provide the source codes of our website on [Github](https://github.com/fwhdzh/FaultFuzz-FrontEnd) so that users can deploy the website by themselves if they want.