package edu.iscas.CCrashFuzzer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSONObject;

public class AflCli {


	public static void main(String[] args) throws AflException{
		interactWithNode(args);
	}

	public static void interactWithNode(String[] args) throws AflException{
		// TODO Auto-generated method stub
		if (args.length < 3) {
			System.out.println("Please specify afl port! [ip] [port] [command:SAVE|STABLE]:" + Arrays.asList(args));
			return;
		}
		String serverIp = args[0].trim();
		int serverPort = Integer.parseInt(args[1].trim());
		String command = args[2].trim();
		// if(!command.equals(AflCommand.SAVE.toString()) &&
		// !command.equals(AflCommand.STABLE.toString())) {
		// System.err.println("Illegal command, should be [SAVE|STABLE]");
		// return;
		// }
		List<String> acdList = Stream.of(AflCommand.values()).map(AflCommand::name).collect(Collectors.toList());
		if (!acdList.contains(command)) {
			System.err.println("Illegal command, should be one of " + JSONObject.toJSONString(acdList));
			return;
		}

		// Socket socket = new Socket(serverIp, serverPort);
		// DataInputStream inStream = new DataInputStream(socket.getInputStream());
		// DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
		// String clientMessage = "", serverMessage = "";
		// clientMessage = command;

		// System.out.println("AflCli send to " + serverIp + ":" + clientMessage);
		// outStream.writeUTF(clientMessage);// request to save results
		// outStream.flush();

		// serverMessage = inStream.readUTF();// get notified
		// System.out.println("From " + serverIp + ", AflCli receive:" + serverMessage);
		// outStream.close();
		// inStream.close();
		// socket.close();
		// if (!serverMessage.equals(AflCommand.FINISH.toString())) {
		// throw new AflException("Save result is not FINISH:" + serverMessage);
		// }
		// System.out.println("AflCli to " + serverIp + " exit!");

		try {
			Socket socket = new Socket(serverIp, serverPort);
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			String clientMessage = "", serverMessage = "";
			clientMessage = command;

			System.out.println("AflCli send to " + serverIp + ":" + clientMessage);
			outStream.writeUTF(clientMessage);// request to save results
			outStream.flush();

			serverMessage = inStream.readUTF();// get notified
			System.out.println("From " + serverIp + ", AflCli receive:" + serverMessage);
			outStream.close();
			inStream.close();
			socket.close();
			if (!serverMessage.equals(AflCommand.FINISH.toString())) {
				throw new AflException("Save result is not FINISH:" + serverMessage);
			}
		} catch (IOException e) {
			// e.printStackTrace();
			// throw e;
			throw new AflException(e.getMessage());
		} finally {
			System.out.println("AflCli to " + serverIp + " exit!");
		}
	}

	public static enum AflCommand {
		HEARTBEAT, // check connection
		NOTREPLAY, // stop replay and do normal execution from now
		DOREPLAY, // start replay from now
		REPLAYMODE, // declare this target is in replay mode
		NORMALMODE, // declare this target is in normal mode
		DETERMINE_CONTROL,
		DETERMINE_NORMAL,
		DETERMINE_NO_SEND,
		SAVE, STABLE, // command
		FINISH, // succ response
		TMOUT, // timeout response
		ILLEGAL // Illegal command
	}

	public static class AflException extends Exception {
		public AflException(String errorMessage) {
			super(errorMessage);
		}
	}


	public static void executeCliCommandToCluster(List<MaxDownNodes> cluster, final Conf conf, AflCommand command, long waitTime) {
		List<Thread> workThreads = new ArrayList<Thread>();
		for (MaxDownNodes subCluster : cluster) {
			for (final String alive : subCluster.aliveGroup) {
				Thread t = new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						String[] args = new String[3];
						args[0] = alive;
						args[1] = String.valueOf(conf.AFL_PORT);
						args[2] = command.toString();
						// args[2] = AflCommand.STABLE.toString();
	
						Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
	
						try {
							interactWithNode(args);
						} catch (AflException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	
				};
				t.start();
				workThreads.add(t);
			}
		}
		for (Thread t : workThreads) {
			try {
				t.join(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static int maxCliCommandTryNumber = 1000;

	public static boolean executeUtilSuccess(List<MaxDownNodes> cluster, final Conf conf, AflCommand command, long waitTime) {
		boolean result = false;
		List<String> aliveList = new ArrayList<String>();
		for (MaxDownNodes subCluster : cluster) {
			for (final String alive : subCluster.aliveGroup) {
				aliveList.add(alive);
			}
		}
		boolean[] success = new boolean[aliveList.size()];
		int[] exeCount = new int[aliveList.size()];
		Arrays.fill(success, false);
		boolean flag = false;
		boolean execTooManyTimes = false;
		while (!flag) {
			for (int i = 0; i < aliveList.size(); i++) {
				if (success[i] == true) {
					continue;
				}
				String alive = aliveList.get(i);
				String[] args = new String[3];
				args[0] = alive;
				args[1] = String.valueOf(conf.AFL_PORT);
				args[2] = command.toString();
				// args[2] = AflCommand.STABLE.toString();
				Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
				try {
					interactWithNode(args);
					success[i] = true;
				} catch (AflException e) {
					// TODO Auto-generated catch block
					success[i] = false;
					exeCount[i]++;

					Stat.debug("Execute AflCli.main with args: " + JSONObject.toJSONString(args) + " failed!");
					Stat.debug(e.getMessage());
					if (exeCount[i] > 0 && exeCount[i] % 10 == 0) {
						Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args) + " failed for " + exeCount[i] + "times");
						Stat.log(e.getMessage());
					}
					if (!e.getMessage().startsWith("Connection refused")) {
						e.printStackTrace();
					}
					// Stat.log(e.getCause().toString());
					
					if (exeCount[i] > maxCliCommandTryNumber) {
						execTooManyTimes = true;
					}
					
				}
			}
			flag = true;
			for (int i = 0; i < success.length; i++) {
				if (!success[i]) {
					flag = false;
					break;
				}
			}
			if (execTooManyTimes) {
				result = false;
				return result;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		result = true;
		Stat.log("executeUtilSuccess with command: " + JSONObject.toJSONString(command) + "success!");
		return result;
	}
}
