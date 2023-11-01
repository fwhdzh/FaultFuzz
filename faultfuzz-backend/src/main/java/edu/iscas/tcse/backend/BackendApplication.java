package edu.iscas.tcse.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.iscas.tcse.backend.ReportReader.BugReportPostParams;

@SpringBootApplication
@RestController
public class BackendApplication {

	public boolean faultFuzzIsRun = false;
	public Thread faultFuzzThread;

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/heartbeat")
	public String heartbeat() {
		System.out.println("heartbeat");
		return "heartbeat";
	}

	@PostMapping("/inst/conf")
	public String handleInstConf(
			@RequestBody InstConf conf) {

		String t = "";
		t = t + "export" + " ";
		t = t + "PHOS_OPTS" + "=" +"\"";
		t = t + "-Xbootclasspath/a:" + conf.instPath + " ";
		t = t + "-javaagent:" + conf.instPath + "=";
		t = t + "useFaultFuzz=" + "false" + ",";
		if (conf.preDefinedInst != null && conf.preDefinedInst.contains("HDFSNetwork")) {
			t = t + "hdfsRpc" + "=true" + ",";
		}
		if (conf.preDefinedInst != null && conf.preDefinedInst.contains("HBaseNetwork")) {
			t = t + "hbaseRpc" + "=true" + ",";
		}
		if (t.charAt(t.length() - 1) == ',') {
			t = t.substring(0, t.length() - 1);
			t = t + "\"";
		}
		t = t + "\n";

		t = t + "export" + " ";
		t = t + "FAV_OPTS" + "=" +"\"";
		t = t + "-Xbootclasspath/a:" + conf.instPath + " ";
		t = t + "-javaagent:" + conf.instPath + "=";
		t = t + "useFaultFuzz=" + conf.useFaultFuzz + ",";
		for (String inst : conf.preDefinedInst) {
			if (inst.equals("AppLevel")) {
				t = t + "useInjectAnnotation" + "=true" + ",";
			} else if (inst.equals("ZKNetwork")) {
				t = t + "forZk" + "=true" + ",";
			} else if (inst.equals("HDFSNetwork")) {
				t = t + "forHdfs" + "=true" + ",";
				t = t + "zkApi" + "=true" + ",";
			} else if (inst.equals("HBaseNetwork")) {
				t = t + "forHbase" + "=true" + ",";
				t = t + "hdfsApi" + "=true" + ",";
				t = t + "zkApi" + "=true" + ",";
			} else {
				t = t + inst + "=true" + ",";
			}
		}

		t = t + "observerHome=" + conf.observerHome + ",";

		// t = t + "recordPath=" + conf.observerHome + "/fav-rst" + ",";
		// t = t + "cacheDir=" + conf.observerHome + "/CacheFolder" + ",";
		// t = t + "covPath=" + conf.observerHome + "/fuzzcov" + ",";

		t = t + "dataPaths=" + conf.dataPaths + ",";
		
		t = t + "controllerSocket=" + conf.controllerSocket + ",";
		// t = t + "mapSize=" + conf.mapSize + ",";
		// t = t + "wordSize=" + conf.wordSize + ",";
		
		t = t + "covIncludes=" + conf.covIncludes + ",";
		t = t + "aflAllow=" + conf.aflAllow + ",";
		t = t + "aflDeny=" + conf.aflDeny + ",";
		t = t + "aflPort=" + conf.aflPort + ",";

		if (conf.annotationFile != null && conf.annotationFile.length() > 0) {
			t = t + "annotationFile=" + conf.annotationFile + ",";
		}

		if (t.charAt(t.length() - 1) == ',') {
			t = t.substring(0, t.length() - 1);
			t = t + "\"";
		}

		System.out.println(t);
		
		return t;
	}

	@PostMapping("/ctrl/conf")
	public String save(
			@RequestBody CtrlConf conf) {
		String s = "";
		s = s + "WORKLOAD=" + conf.workload + "\n";
		s = s + "CHECKER=" + conf.checker + "\n";
		s = s + "FAULT_TYPE=[" + conf.faultType.stream().collect(Collectors.joining(",")) + "]" + "\n";
		s = s + "CRASH=" + conf.crash + "\n";
		s = s + "REBOOT=" + conf.reboot + "\n";
		s = s + "NETWORK_DISCONNECTION=" + conf.networkDisconnection + "\n";
		s = s + "NETWORK_RECONNECTION=" + conf.networkReconnection + "\n";
		s = s + "ROOT_DIR=" + conf.rootDir + "\n";
		s = s + "CUR_FAULT_FILE=" + conf.currentFaultFile + "\n";
		s = s + "CONTROLLER_PORT=" + conf.controllerPort + "\n";
		s = s + "MONITOR=" + conf.monitor + "\n";
		s = s + "PRETREATMENT=" + conf.preTreatment + "\n";
		s = s + "TEST_TIME=" + conf.testTime + "\n";
		s = s + "FAULT_CSTR=" + conf.faultCluster + "\n";
		s = s + "AFL_PORT=" + conf.aflPort + "\n";
		s = s + "HANG_TIMEOUT=" + conf.hangTimeOut + "\n";
		s = s + "MAX_FAULTS=" + conf.maxFaults + "\n";
		s = s + "DETERMINE_WAIT_TIME=" + conf.determineWaitTime + "\n";
		System.out.println(s);
		return s;
	}

	public String readBugReportPathFromCtrlConfFile(File ctrlConfFile) throws IOException {
		String bugReportPath = null;
		List<String> lines = null;
		lines = Files.readAllLines(ctrlConfFile.toPath());
		String rootPath = null;
		for (String line : lines) {
			if (line.startsWith("ROOT_DIR")) {
				rootPath = line.substring(line.indexOf("=") + 1);
				break;
			}
		}
		bugReportPath = rootPath + "/TEST_REPORT";
		return bugReportPath;
	}

	public String readPretreatmentPathFromCtrlConfFile(File ctrlConfFile) throws IOException {
		String pretreatmentPath = null;
		List<String> lines = null;
		lines = Files.readAllLines(ctrlConfFile.toPath());
		for (String line : lines) {
			if (line.startsWith("PRETREATMENT")) {
				pretreatmentPath = line.substring(line.indexOf("=") + 1);
				break;
			}
		}
		if (pretreatmentPath == null) {
			return null;
		}
		if (!pretreatmentPath.startsWith("/")){
			File workDir = ctrlConfFile.getParentFile();
			pretreatmentPath = workDir.getAbsolutePath() + "/" + pretreatmentPath;
		}
		return pretreatmentPath;
	}

	public String readRootPathFromCtrlConfFile(File ctrlConfFile) throws IOException {
		String rootPath = null;
		List<String> lines = null;
		lines = Files.readAllLines(ctrlConfFile.toPath());
		for (String line : lines) {
			if (line.startsWith("ROOT_DIR")) {
				rootPath = line.substring(line.indexOf("=") + 1);
				break;
			}
		}
		if (rootPath == null) {
			return null;
		}
		if (!rootPath.startsWith("/")){
			File workDir = ctrlConfFile.getParentFile();
			rootPath = workDir.getAbsolutePath() + "/" + rootPath;
		}
		return rootPath;
	}

	public String ctrlJarPath;
	public String ctrlPropertiesPath;

	public String bugReportPath;
	public String pretreatmentPath;
	public String rootPath;

	@PostMapping("/begin/test")
	public String beginTest(@RequestBody BugReportPostParams params) throws IOException {

		if (faultFuzzIsRun || (faultFuzzThread != null && faultFuzzThread.isAlive())) {
			System.out.println("FaultFuzz is running! please stop it first!");
			return "FaultFuzz is running! please stop it first!";
		}

		ctrlJarPath = params.ctrlJarPath;
		ctrlPropertiesPath = params.ctrlPropertiesPath;

		String s = "";
		s = s + "java";
		s = s + " " + "-cp" + " " + params.ctrlJarPath;
		s = s + " " + "edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain";
		s = s + " " + params.ctrlPropertiesPath;
		System.out.println(s);

		final String beginCommand = s;
		final File tmpFile = new File("evaluation-process");
		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		try {
			tmpFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(params.ctrlPropertiesPath);
		if (!file.exists()) {
			throw new RuntimeException("properties file not exist!");
		}
		File workDir = file.getParentFile();

		bugReportPath = readBugReportPathFromCtrlConfFile(file);

		if (bugReportPath == null || bugReportPath.length() == 0) {
			throw new RuntimeException("bug report path is null!");
		}
		System.out.println("bugReportPath is: " + bugReportPath);

		pretreatmentPath = readPretreatmentPathFromCtrlConfFile(file);
		if (pretreatmentPath == null || pretreatmentPath.length() == 0) {
			throw new RuntimeException("pretreatment path is null!");
		}
		System.out.println("pretreatmentPath is: " + pretreatmentPath);

		rootPath = readRootPathFromCtrlConfFile(file);
		if (rootPath == null || rootPath.length() == 0) {
			throw new RuntimeException("rootPath path is null!");
		}
		System.out.println("rootPath is: " + rootPath);

		Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				// List<String> result = RunCommand.run(beginCommand,
				// "/data/fengwenhan/code/faultfuzz/package/zk-3.6.3", tmpFile);

				List<String> result = RunCommand.run(beginCommand,
						workDir.getAbsolutePath(), tmpFile);
				faultFuzzIsRun = false;
			}
		};
		faultFuzzIsRun = true;
		faultFuzzThread = t;
		t.start();
		return beginCommand;
	}

	static class StopTestParams {
		public String pretreatment;
	}

	@PostMapping("/stop/test")
	public String stopTest() {
		String rootPath = System.getProperty("user.dir");
		File clearMaster = new File(rootPath + "/scripts/clear-master-process.sh");
		System.out.println("clearMaster path: " + clearMaster.getAbsolutePath());

		if (faultFuzzIsRun && (faultFuzzThread != null && faultFuzzThread.isAlive())) {
			faultFuzzThread.interrupt();
			// String path = params.pretreatment;
			Thread clearThread = new Thread() {
				@Override
				public void run() {
					// RunCommand.run(path);
					RunCommand.run(pretreatmentPath);
					RunCommand.run(clearMaster.getAbsolutePath());
				}
			};
			clearThread.start();

			faultFuzzIsRun = false;
			return "stop test";
		} else {
			return "test is not running";
		}
	}

	int i = 0;

	@GetMapping("/report")
	public String getReport() throws IOException {
		
		// String path = params.bugReportLocation;
		String path = bugReportPath;
		ReportReader rr = new ReportReader();
		String s = rr.readReport(path);
		System.out.println(s);
		return s;
	}

	static class pauseTesttPostParams {
		public String rootDir;
	}

	@PostMapping("/pause")
	public String pauseTest(@RequestBody pauseTesttPostParams params) {
		String rootDir = params.rootDir;
		File f = new File(rootDir + "/PAUSE");
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String s = "pause test";
		System.out.println(s);
		return s;
	}

	@PostMapping("/resume")
	public String resumeTest(@RequestBody pauseTesttPostParams params) {

		System.out.println("Remove PAUSE file......");
		String rootDir = params.rootDir;
		File f = new File(rootDir + "/PAUSE");
		f.delete();

		if (faultFuzzIsRun || (faultFuzzThread != null && faultFuzzThread.isAlive())) {
			System.out.println("FaultFuzz is running! just resume it by removing PAUSE file!");
			String s = "resume test";
			System.out.println(s);
			return s;
		}
		
		String s = "";
		s = s + "java";
		s = s + " " + "-cp" + " " + ctrlJarPath;
		s = s + " " + "edu.iscas.tcse.faultfuzz.ctrl.CloudFuzzMain";
		s = s + " " + ctrlPropertiesPath;
		s = s + " " + "recover";
		System.out.println(s);

		final String beginCommand = s;
		final File tmpFile = new File("evaluation-process");
		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		try {
			tmpFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(ctrlPropertiesPath);
		if (!file.exists()) {
			throw new RuntimeException("properties file not exist!");
		}
		File workDir = file.getParentFile();

		Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				// List<String> result = RunCommand.run(beginCommand,
				// "/data/fengwenhan/code/faultfuzz/package/zk-3.6.3", tmpFile);

				List<String> result = RunCommand.run(beginCommand,
						workDir.getAbsolutePath(), tmpFile);
				faultFuzzIsRun = false;
			}
		};
		faultFuzzIsRun = true;
		faultFuzzThread = t;
		t.start();
		return beginCommand;

		
	}

	public int replayCount = 0;

	static class ReplayPostParams {
		public String replayCtrlJarPath;
		public String replayConfPath;
		public String favRSTPath;
	}

	static class ReplayPostResponse {
		public int id;
		public String info;

		public ReplayPostResponse(int id, String info) {
			this.id = id;
			this.info = info;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getInfo() {
			return info;
		}
		public void setInfo(String info) {
			this.info = info;
		}

		
	}

	@PostMapping("/replay")
	public String replay(@RequestBody ReplayPostParams params) throws JsonProcessingException {
		if (faultFuzzIsRun || (faultFuzzThread != null && faultFuzzThread.isAlive())) {
			System.out.println("FaultFuzz is running! please stop it first!");
			return "FaultFuzz is running! please stop it first!";
		}
		File reportFile = new File("replay-report");
		if (reportFile.exists()) {
			reportFile.delete();
		}
		String s = "";
		s = s + "java";
		s = s + " " + "-cp" + " " + params.replayCtrlJarPath;
		s = s + " " + "edu.iscas.tcse.faultfuzz.ctrl.replay.Replayer";
		s = s + " " + params.replayConfPath;
		s = s + " " + params.favRSTPath;
		s = s + " " + reportFile.getAbsolutePath();

		replayCount++;
		System.out.println("replayCount: " + replayCount);
		ReplayPostResponse response = new ReplayPostResponse(replayCount, s);
		ObjectMapper mapper = new ObjectMapper();
		String responseString = mapper.writeValueAsString(response);
		 
		System.out.println(responseString);
		// return responseString;

		final String replayCommand = s;
		final File replayTmpFile = new File("replay-process");
		if (replayTmpFile.exists()) {
			replayTmpFile.delete();
		}
		try {
			replayTmpFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(params.replayCtrlJarPath);
		if (!file.exists()) {
			throw new RuntimeException("properties file not exist!");
		}
		File workDir = file.getParentFile();

		Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				// List<String> result = RunCommand.run(replayCommand,
				// 		"/data/fengwenhan/code/faultfuzz/package/zk-3.6.3", replayTmpFile);
				List<String> result = RunCommand.run(replayCommand,
						workDir.getAbsolutePath(), replayTmpFile);
				faultFuzzIsRun = false;
				System.out.println("replay is finished!");
			}
		};

		faultFuzzIsRun = true;
		faultFuzzThread = t;

		t.start();
		return responseString;
		
	}

	@PostMapping("/replay/stop")
	public String stopRepaly() {
		String rootPath = System.getProperty("user.dir");
		File clearReplay = new File(rootPath + "/scripts/clear-replay-process.sh");
		System.out.println(clearReplay.getAbsolutePath());
		if (faultFuzzIsRun && (faultFuzzThread != null && faultFuzzThread.isAlive())) {
			faultFuzzThread.interrupt();
			Thread clearThread = new Thread() {
				@Override
				public void run() {
					// RunCommand.run(path);
					// RunCommand.run("/data/fengwenhan/code/faultfuzz/faultfuzz-backend/scripts/clear-replay-process.sh");
					RunCommand.run(clearReplay.getAbsolutePath());
				}
			};
			clearThread.start();
			faultFuzzIsRun = false;
			return "stop replay";
		} else {
			return "replay is not running";
		}
	}

	static class ReplayReportGetParams {
		public int id;
	}

	@GetMapping("/replay/report")
	public String replayReport(@RequestBody ReplayReportGetParams params) {
		System.out.println("handle replay report request");
		System.out.println("params.id = " + params.id);
		System.out.println("replayCount: " + replayCount);
		int id = params.id;
		if (id != replayCount) {
			return "Cannot find replay task";
		}
		File reportFile = new File("replay-report");
		if (!reportFile.exists()) {
			return "replay report not exist, it may have not been generated yet because replay task is still running.";
		}
		String s = "";
		s = s + "The detail of replay report can be found in: " + reportFile.getAbsolutePath() + "\n";
		try {
			BufferedReader br = new BufferedReader(new FileReader(reportFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				s = s + line + "\n";
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(s);
		return s;
	}

	

}
