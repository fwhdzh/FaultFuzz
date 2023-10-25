package edu.iscas.tcse.backend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.iscas.tcse.backend.ReportReader.BugReportGetParams;
import edu.iscas.tcse.backend.ReportReader.BugReportPostParams;

@SpringBootApplication
@RestController
public class BackendApplication {

	public boolean isTest = false;
	public Thread currentTestThread;

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
		t = t + "useFaultFuzz=" + "false" + "\"";
		t = t + "\n";

		t = t + "export" + " ";
		t = t + "FAV_OPTS" + "=" +"\"";
		t = t + "-Xbootclasspath/a:" + conf.instPath + " ";
		t = t + "-javaagent:" + conf.instPath + "=";
		t = t + "useFaultFuzz=" + conf.useFaultFuzz + ",";
		for (String inst : conf.preDefinedInst) {
			t = t + inst + "=true" + ",";
		}
		t = t + "recordPath=" + conf.recordPath + ",";
		t = t + "dataPaths=" + conf.dataPaths + ",";
		t = t + "cacheDir=" + conf.cacheDir + ",";
		t = t + "controllerSocket=" + conf.controllerSocket + ",";
		t = t + "mapSize=" + conf.mapSize + ",";
		t = t + "wordSize=" + conf.wordSize + ",";
		t = t + "covPath=" + conf.covPath + ",";
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
		s = s + "REBOOT=" + conf.reboot;
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

	@PostMapping("/begin/test")
	public String beginTest(@RequestBody BugReportPostParams params) {

		if (isTest || (currentTestThread != null && currentTestThread.isAlive())) {
			System.out.println("test is running");
			return "test is running";
		}

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

		Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				List<String> result = RunCommand.run(beginCommand,
						"/data/fengwenhan/code/faultfuzz/package/zk-3.6.3", tmpFile);
				// try {
				// FileOutputStream fos = new FileOutputStream(tmpFile);
				// for (String line : result) {
				// fos.write(line.getBytes());
				// fos.write("\n".getBytes());
				// }
				// fos.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// for (String line : result) {
				// System.out.println(line);
				// }
			}
		};
		isTest = true;
		currentTestThread = t;
		t.start();
		return beginCommand;
	}

	static class StopTestParams {
		public String pretreatment;
	}

	@PostMapping("/stop/test")
	public String stopTest(@RequestBody StopTestParams params) {
		if (isTest && (currentTestThread != null && currentTestThread.isAlive())) {
			currentTestThread.interrupt();
			String path = params.pretreatment;
			Thread clearThread = new Thread() {
				@Override
				public void run() {
					RunCommand.run(path);
					RunCommand.run("/data/fengwenhan/code/faultfuzz/faultfuzz-backend/scripts/clear-master-process.sh");
				}
			};
			clearThread.start();

			isTest = false;
			return "stop test";
		} else {
			return "test is not running";
		}
	}

	int i = 0;

	@GetMapping("/report")
	public String getReport(@RequestBody BugReportGetParams params) throws IOException {
		String path = params.bugReportLocation;
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
		String rootDir = params.rootDir;
		File f = new File(rootDir + "/PAUSE");
		f.delete();
		String s = "resume test";
		System.out.println(s);
		return s;
	}

}
