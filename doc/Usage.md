# FaultFuzz

<!-- 概述（Abstract） -->
FaultFuzz是一款用于分布式系统健壮性测试的工具。FaultFuzz使用了Fuzzing技术，可以在目标系统的I/O点粒度上进行系统性的故障注入测试。 

<!-- 论文（paper） -->
有关FaultFuzz的算法设计的更多信息，可以查看下列论文：
- Coverage Guided Fault Injection for Cloud Systems
- FaultFuzz: Coverage Guided Fault Injection for Distributed Systems (投稿中)

<!-- 项目结构 -->
FaultFuzz由多个子项目组成。具体来说，FaultFuzz包含
- 一个用于收集目标系统运行时信息的java命令行程序 （FaultFuzz-inst）
- 一个用于控制测试流程的java命令行程序 (FaultFuzz-ctrl)
- 一个为控制程序提供可视化界面的Spring boot服务器程序(FaultFuzz-Backend)以及AppSmith网页程序 (FaultFuzz-FrontEnd)
- 以及一系列在我们之前的论文实验中所使用过的测试例子 (FaultFuzz-workloads)

<!-- FaultFuzz是如何运行的？ -->
在准备好运行环境后，FaultFuzz分为4个部分。

首先，FaultFuzz会不注入任何故障运行一次目标系统。在这次运行当中，FaultFuzz会收集系统所到达的所有I/O点。在这次运行之后，FaultFuzz会得到一个能够反映系统运行历史的I/O点序列。

之后，FaultFuzz会对该I/O序列进行变异。具体来说，其会选择一个I/O点，向其注入一个新的故障。这样我们就得到了一个故障序列，其代表着系统应该首先依次到达该I/O点之前的所有I/O点，然后在到达该I/O点时，遭遇了我们注入的故障。FaultFuzz的变异是系统性的，这意味着我们在这一步会生成所有可能的故障序列。

然后，FaultFuzz会通过其内置的策略选择下一个最容易测试到系统新的行为的错误序列。具体的选择策略可以参考论文。

最后，FualtFuzz会根据选定的错误序列来进行下一次的错误序列测试。FaultFuzz会控制系统的行为，当系统到达需要注入故障的I/O点时，系统会对其进行故障注入。

在这一次注入了故障的测试中，FaultFuzz又会观测到新的系统行为，从而再次进行变异-选择-测试的循环。FaultFuzz的测试会一直持续到无法再变异出没有测试过的错误序列为止。用户也可以指定测试总时长等测试条件。

<!-- 相比于CrashFuzz的改进 -->

<!-- 如何使用（How to Use?） -->
用户需要使用以下步骤来使用FaultFuzz:

1. 安装FaultFuzz

```
git clone https://github.com/fwhdzh/FaultFuzz.git
cd faultfuzz
mvn clean install
```

1. 启动FaultFuzz server.
```
cd faultfuzz-backend
mvn spring-boot:run
```

1. （可选）准备插装过的JDK

FaultFuzz内置了jdk层次的文件I/O和消息I/O拦截机制。如果用户需要，可以通过简单的配置开启该功能。

如果用户需要该功能，则需要对JDK进行插装形成产生具有相关机制的JDK。

（考虑通过直接提供插装过的jdk而非由用户插装的方式）

```
java -jar FaultFuzz-inst.jar -forJava <jdk_path> <instrument_jdk_path>

rm <instrument_jdk_path>/jre/lib/jce.jar
cp <our_provided_jre>/jre/lib/jce.jar <instrument_jdk_path>/jre/lib
rm -r <instrument_jdk_path>/jre/lib/security/policy/*
cp -r <our_provided_jre>/jre/lib/security/policy/* <instrument_jdk_path>/jre/lib/security/policy
chmod +x <instrument_jdk_path>/bin/*
```
之后我们可以将$JAVA_HOME设置为<instrument_jdk_path>，从而使得之后的java命令可以被翻译为<instrument_jdk_path>/bin/java。

3. （可选）准备用户自定义的I/O点。

FaultFuzz允许用户自己标注待测试系统中的I/O点。FaultFuzz提供了使用注解的的标注和使用API的标注。

当用户想要自己标注I/O点时，其首先需要在项目中添加对FaultFuzz-inst的依赖。如果目标系统基于maven结构，其可以在pom.xml中添加。
```
<dependencies>
    <dependency>
        <groupId>edu.iscas.tcse</groupId>
        <artifactId>FaultFuzz-inst</artifactId>
        <version>0.0.5-SNAPSHOT</version>
    </dependency>
</dependencies>
```
对于其他系统，也可以通过直接指定FaultFuzz-inst.jar路径等方式引用依赖。

如果用户想要将系统的某一个位置视为I/O点，其可以调用`WaitToExec.triggerAndRecordFaultPoint(String path);`的API进行标注。其中path是FaultFuzz用于识别该I/O点的额外信息，一般可以设置为文件路径或消息内容。

一个例子：
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

FaultFuzz会将上面例子中的第六行视为一个I/O点。

如果用户想要将系统的某一个函数视为I/O函数（系统中所有对函数的调用都将被识为I/O点），则其可以在该函数上添加@Inject注解。

一个例子：
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

在这个例子中，由于startConnection被添加了@Inject标注，所以main函数的第二行对startConnection的调用会被视为一个I/O点。

当用户完成所有的标注后，用户需要使用
```
java -cp FaultFuzz-inst.jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier <target_project_path> <info_file>
```
命令来将相关的信息提取到<info_file>里。在之后的测试流程中，用户可以通过配置告诉FaultFuzz使用<info_file>里储存的信息。

4. 创建FaultFuzz配置文件。

在运行FaultFuzz之前，用户需要对FaultFuzz进行一定的配置。我们提供了UI界面来使得用户更方便地生成相关配置文件。

FaultFuzz-ctrl支持的配置项包括

```
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
```

FaultFuzz-inst支持的配置项包括
```
useFaultFuzz: 对该程序（进程）使用FaultFuzz进行控制

controllerSocket: FaultFuzz-ctrl的服务套接字。
aflPort：FaultFuzz-inst用于和FaultFuzz-ctrl进行网络通信的端口。

recordPath：FaultFuzz记录中间数据的位置。
dataPaths：目标系统的数据位置。(似乎只在jdk插装时有用，)
cacheDir：插装的缓存数据记录位置，记录插装缓存数据可以提升FaultFuzz的性能并提供更多的调试信息。

mapSize：FaultFuzz的块覆盖率统计使用的内村总大小。
workSize：FaultFuzz对每一个代码块的覆盖率统计信息所使用的内存大小。
covPath：FaultFuzz的覆盖率统计数据存放位置。
covIncludes：FaultFuzz只会统计该配置项指定的前缀的类的覆盖率信息。

aflAllow：FaultFuzz只会插装该文件中所指定前缀的类。
aflDeny：FaultFuzz不会插装该文件中所指定前缀的类。

useMsgid：对系统中发送的消息赋予MessageId，从而使得FaultFuzz能够匹配发送端和接收端的消息，优化测试流程。
jdkFile：使用FaultFuzz内置的JDK层文件I/O插装机制。对JDK的文件读写操作进行插装。
jdkMsg：使用FaultFuzz内置的JDK层消息I/O插装机制。对JDK的消息读写操作进行插装。
forZk：使用FaultFuzz内置的Zookeeper应用层I/O插装机制，对Zookeeper进行插装。
zkApi: 使用FaultFuzz内置的Zookeeper应用层I/O插装机制，对Zookeeper暴露给上层系统的API接口进行插装。
forHdfs：使用FaultFuzz内置的HDFS应用层I/O插装机制，对HDFS进行插装。
hdfsApi：使用FaultFuzz内置的HDFS应用层I/O插装机制，对HDFS暴露给上层系统的API接口进行插装。
forHBase: 使用FaultFuzz内置的HBase应用层I/O插装机制，对HBase进行插装。
```

用户可以在我们的可视化界面查看相关配置，并通过点击按钮将其生成到指定的位置。

FaultFuzz会为你生成两个文件，命名为ctrl.properties和fav_env.sh。
其中fav_env.sh会提供两个linux环境变量。
```
export PHOS_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=false,useMsgid=false,jdkMsg=false"

export FAV_OPTS="-Xbootclasspath/a:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar -javaagent:/home/gaoyu/FaultFuzz-inst-0.0.5-SNAPSHOT.jar=useFaultFuzz=true,forZk=true,useMsgid=false,jdkMsg=false,jdkFile=true,recordPath=/home/gaoyu/zk363-fav-rst/,dataPaths=/home/gaoyu/evaluation/zk-3.8.1/zkData/version-2,cacheDir=/home/gaoyu/CacheFolder,controllerSocket=172.30.0.1:12090,mapSize=10000,wordSize=64,covPath=/home/gaoyu/fuzzcov,covIncludes=org/apache/zookeeper,aflAllow=/home/gaoyu/evaluation/zk-3.8.1/allowlist,aflDeny=/home/gaoyu/evaluation/zk-3.8.1/denylist,aflPort=12081,execMode=FaultFuzz"
```
其中FAV_OPTS是根据用户定义的配置所生成的环境变量。而PHOS_OPTS则是在FAV_OPTS的基础上保留由FaultFuzz引入的额外信息，但是不进行控制的环境变量。PHOS_OPTS在由多个进程共同作用的分布式系统中十分有用。比如，对于Zookeeper，我们可能希望server进程受到FaultFuzz的控制，而client进程则不受到控制。

5. 部署集群。

用户需要自行部署自己的测试集群，并将相关配置项加入到集群的启动文件上。

一般来说，用户只需要在原本的集群的节点的启动文件上加入对应的参数。

比如在Zookeeper中，加入方法为修改zkEnv.sh文件，在Zookeeper启动所使用的SERVER_JVMFLAGS和CLIENT_JVMFLAGS中加入FAV_OPTS和PHOS_OPTS

```
export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS $FAV_OPTS $TIME_OPTS"

export CLIENT_JVMFLAGS="-Xmx${ZK_CLIENT_HEAP}m $CLIENT_JVMFLAGS $PHOS_OPTS $TIME_OPTS"

```

6. 启动测试并观测结果

用户可以在界面上点击测试，FaultFuzz会自动在测试上展现测试结果。