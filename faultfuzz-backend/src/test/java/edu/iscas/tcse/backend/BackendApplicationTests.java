package edu.iscas.tcse.backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.iscas.tcse.backend.BackendApplication.ReplayPostResponse;

@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testJackson() {
		ReplayPostResponse r = new ReplayPostResponse(1, "test");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(r);
			System.out.println(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Test
	void testReadBugReportPathFromCtrlConfFile() throws IOException {
		// BackendApplication app = new BackendApplication();
		// File configurationFile = new File("/data/fengwenhan/code/faultfuzz/package/zk-3.6.3/zk.properties");
		// String bugReportPath = app.readBugReportPathFromCtrlConfFile(configurationFile);
		// System.out.println(bugReportPath);
	}

	@Test
	void testSystemDir() throws IOException {
		String rootPath = System.getProperty("user.dir");
		System.out.println("Project root directory: " + rootPath);
		File clearMaster = new File(rootPath + "/scripts/clear-master-process.sh");
		assertTrue(clearMaster.exists());
		File clearReplay = new File(rootPath + "/scripts/clear-replay-process.sh");
		assertTrue(clearReplay.exists());
	}

	@Test
	void testReadPretreatmentPathFromCtrlConfFile() throws IOException {
		// BackendApplication app = new BackendApplication();
		// File configurationFile = new File("/data/fengwenhan/code/faultfuzz/package/zk-3.6.3/zk.properties");
		// String bugReportPath = app.readPretreatmentPathFromCtrlConfFile(configurationFile);
		// System.out.println(bugReportPath);
	}
}
