1. Download the pre-deployed Zookeeper cluster.

See [INSTALL.md](./INSTALL.md)

2. Install FaultFuzz.

```
git clone https://github.com/fwhdzh/FaultFuzz.git
cd faultfuzz
mvn clean install
```

3. Launch the FaultFuzz server.
```
cd faultfuzz-backend
mvn spring-boot:run
```

4. Open the frontend website, and fill configurations.
```
Ctrl_Server: the ipaddress and port of faultfuzz-backend server. In format like "192.168.112.70:8080"
Ctrl_Jar_Path: the location of FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar in the server.
Ctrl_Properties_Path: the location of zk.properties in the server.
Bug_Report_Location: the location of TEST_REPORT in the server.
TestTime: testTime;
```

5. click "start test" button, and you will see the bug report after a while.