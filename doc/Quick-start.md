We have prepared a Zookeeper cluster based on Docker and completed most of the
configuration and preparation work within the Docker containers. Users can
quickly experience FaultFuzz through this cluster.


### Step 1: Install Zookeeper cluster.

Users can install the Zookeeper cluster by the document [INSTALL.md](INSTALL.md). Note that the server used to run FaultFuzz should satisfy the requirements in [REQUIREMENTS.md](REQUIREMENTS.md).

### Step 2: Install FaultFuzz and start FaultFuzz server.


```
git clone https://github.com/fwhdzh/FaultFuzz.git

cd faultfuzz
mvn clean install

cd faultfuzz-backend
mvn spring-boot:run
```

We provide a visual frontend as a website on [AppSmith
cloud](https://app.appsmith.com/app/faultfuzz/readme-652b42d079d5b0084315e511?branch=master)
cloud. Users can go to the “Check connection” web page, enter the address of the
web server, and click the "Check connection" button to confirm that the web
server has been started and the frontend can connect to the web server.

<div style="text-align:center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-check-connection.png" style="width:70%">
</div>
<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-check-connection.png) -->

Then the user can access the test server through the frontend of FaultFuzz. After inputting the address of the test server, the user
can click the "check connection" button to confirm that the backend has been started and the frontend can correctly
connect to the backend. 

### Step 3: Configure FaultFuzz and SUT.

We provide a
``Configuration`` web page (see \figref{fig:screenshot}-\circled{2}) for
users to specify the configurations used to test a target distributed
system. The configurations can be divided into four categories, i.e.,
``Workloads & bug checker``, ``Faults & fault injection points``,
``Observer`` and ``Test controlle``.

<div style="text-align:center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-configuration.png" style="width:70%">
</div>

<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-configuration.png) -->

We provide a button ``Use pre-defined Zookeeper configuration`` in our website to fill most of our zookeeper's configuration options to this
page. The left configuration options are where the scripts are. We have provided all scripts needed
in our artifact, users can download them and specify the paths on this page.

<div style="text-align:center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-configuration-button.png" style="width:60%">
</div>

<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-configuration-button.png) -->

After entering the above configuration information, users can click the
``Generate configuration files'' button to generate and download two
configuration files, named FaultFuzz-backend-configuration.properties and FaultFuzz-SUT-configuration.sh.

The file FaultFuzz-backend-configuration.properties should be uploaded to the backend of FaultFuzz. 
The FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar will use FaultFuzz-backend-configuration.properties as an input argument.

The FaultFuzz-SUT-configuration.sh should be copied to each node in SUT. In our Zookeeper cluster, we have prepared the environment already.
So the only thing the users need to do is copy FaultFuzz-SUT-configuration.sh to `/SUT-configuration` folder of each docker container.

### Step 4: Start the testing and observe the results.

After finishing configuration, the user can go to the ``Test and result``
page, enter the path of FaultFuzz test controller jar file (FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar) and the path of the configuration file (FaultFuzz-backend-configuration.properties). When the user clicks the ``Start
test`` button, FaultFuzz will automatically perform fault injection testing for SUT. Users can also pause, resume or stop the test by clicking the corresponding buttons.

<div style="text-align:center">
    <img src="https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-test-and-result.png" style="width:70%">
</div>

<!-- ![](https://raw.githubusercontent.com/fwhdzh/pic/main/FaultFuzz-page-test-and-result.png) -->

FaultFuzz displays quantitative statistics of the runtime test results at the bottom of the web page, including the elapsed testing time, the total number of detected bugs, the total number of tested fault sequences, the
total number of covered basic code blocks and so on.
If the user wants to further observe one specific bug, she can check the detailed bug reports. The user can also try to replay a bug by entering the file path of the fault sequence that triggers the bug and clicking the
``Start replay`` button.



<!-- ```

JAVA="<instrumented_JRE_HOME>/bin/java"

. <your_configuration_folder>/FaultFuzz-SUT-configuration.sh

export SERVER_JVMFLAGS="-Xmx${ZK_SERVER_HEAP}m $SERVER_JVMFLAGS $FAV_OPTS $TIME_OPTS"
export CLIENT_JVMFLAGS="-Xmx${ZK_CLIENT_HEAP}m $CLIENT_JVMFLAGS $PHOS_OPTS $TIME_OPTS"

```

If you are interesting in the meaning of each configuration item in these configuration files, please refer to our configuration documentation. Note that the configurations in these files are more low-level and slightly different from the configuration items provided in our configuration interface. In the configuration interface, we have simplified the configuration information to make it more human-understandable. -->

