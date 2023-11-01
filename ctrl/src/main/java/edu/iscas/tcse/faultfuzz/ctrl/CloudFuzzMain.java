package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.commons.io.FileUtils;

import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class CloudFuzzMain {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length < 1) {
			System.out.println("Please specify the configuration file!");
			return;
		}
		
		File confFile = new File(args[0]);
		if(!confFile.exists()) {
			System.out.println("The configuration file does not exist!");
			return;
		}

		Conf conf = new Conf(confFile);
		// conf.CONTROLLER_PORT = Integer.parseInt(args[0].trim());
		conf.loadConfigurationAndCheckAndPrint();

		if ((args.length == 2) && (args[1].equals("recover"))) {
			conf.RECOVERY_MODE = true;
		}
		
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //System.out.println(runtimeMXBean.getName());
        int myproc = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
		FileUtils.writeByteArrayToFile(new File(FileUtil.root+FileUtil.fuzzer_id_file), String.valueOf(myproc).getBytes());

		Fuzzer fuzzer = new Fuzzer(conf);
		fuzzer.start();
	}
}
