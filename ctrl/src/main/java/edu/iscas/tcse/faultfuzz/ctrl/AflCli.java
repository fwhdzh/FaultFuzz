package edu.iscas.tcse.faultfuzz.ctrl;

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

	public static enum AflCommand {
		HEARTBEAT, // check connection
		NOTREPLAY, // stop replay and do normal execution from now
		DOREPLAY, // start replay from now
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

	public static int maxCliCommandTryNumber = 150;

	public static void interactWithNode(String serverIp, int serverPort, String command) throws AflException {
		List<String> acdList = Stream.of(AflCommand.values()).map(AflCommand::name).collect(Collectors.toList());
		if (!acdList.contains(command)) {
			System.err.println("Illegal command, should be one of " + JSONObject.toJSONString(acdList));
			return;
		}
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
			Stat.debug("AflCli to " + serverIp + " exit!");
		}
	}

	public static void interactWithNode(String[] args) throws AflException {
		if (args.length < 3) {
			System.out.println("Please specify afl port! [ip] [port] [command:SAVE|STABLE]:" + Arrays.asList(args));
			return;
		}
		String serverIp = args[0].trim();
		int serverPort = Integer.parseInt(args[1].trim());
		String command = args[2].trim();
		interactWithNode(serverIp, serverPort, command);
	}

	public static void executeCliCommandToCluster(List<MaxDownNodes> cluster, int aflPort, AflCommand command,
			long waitTime) {
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
						args[1] = String.valueOf(aflPort);
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

	public static void main(String[] args) throws AflException {
		interactWithNode(args);
	}

	public static List<String> getAliveNodesInCluster(List<MaxDownNodes> cluster) {
		List<String> aliveList = new ArrayList<String>();
		for (MaxDownNodes subCluster : cluster) {
			for (final String alive : subCluster.aliveGroup) {
				aliveList.add(alive);
			}
		}
		return aliveList;
	}

	public static boolean executeUtilSuccess(List<MaxDownNodes> cluster, int aflPort, AflCommand command,
			long waitTime) {
		boolean result;
		List<String> aliveList = getAliveNodesInCluster(cluster);
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
				args[1] = String.valueOf(aflPort);
				args[2] = command.toString();
				if (exeCount[i] == 0) {
					Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
				} else {
					Stat.debug("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
				}

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
						Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args) + " failed for "
								+ exeCount[i] + "times");
						Stat.log(e.getMessage());
					}
					if (!e.getMessage().contains("Connection refused")) {
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

	

	public static boolean executeUtilSuccess(String[] args) {
		boolean success = false;
		int exeCount = 0;
		boolean execTooManyTimes = false;
		while ((!success) && (!execTooManyTimes)) {
			if (exeCount == 0) {
				Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
			} else {
				Stat.debug("Execute AflCli.main with args: " + JSONObject.toJSONString(args));
			}

			try {
				interactWithNode(args);
				success = true;
			} catch (AflException e) {
				// TODO Auto-generated catch block
				success = false;
				exeCount++;

				Stat.debug("Execute AflCli.main with args: " + JSONObject.toJSONString(args) + " failed!");
				Stat.debug(e.getMessage());
				if (exeCount > 0 && exeCount % 10 == 0) {
					Stat.log("Execute AflCli.main with args: " + JSONObject.toJSONString(args) + " failed for "
							+ exeCount + "times");
					Stat.log(e.getMessage());
				}
				if (!e.getMessage().contains("Connection refused")) {
					e.printStackTrace();
				}
				// Stat.log(e.getCause().toString());

				if (exeCount > maxCliCommandTryNumber) {
					execTooManyTimes = true;
				}

			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean result = success;
		return result;
	}
}
