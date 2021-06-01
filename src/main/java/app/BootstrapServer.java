package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BootstrapServer {

	private volatile boolean working = true;
	private final List<BootstrapServentInfo> activeServentsInfo;

	private void run() {
		Scanner sc = new Scanner(System.in);

		String line;
		while(true) {
			line = sc.nextLine();

			if (line.equals("stop")) {
				working = false;
				break;
			}
		}

		sc.close();
	}
	
	public BootstrapServer() {
		activeServentsInfo = new ArrayList<>();
	}
	
	public void doBootstrap(int bsPort) {

		Thread cliThread = new Thread(this::run);
		cliThread.start();

		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(bsPort);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			AppConfig.timestampedErrorPrint("Problem while opening listener socket.");
			e1.printStackTrace();
			System.exit(0);
		}
		
		Random rand = new Random(System.currentTimeMillis());
		
		while (working) {
			try {
				Socket newServentSocket = listenerSocket.accept();

				String newNodeSocketAddress = newServentSocket.getRemoteSocketAddress().
						toString().
						replace("/", "").
						split(":")[0];

				Scanner socketScanner = new Scanner(newServentSocket.getInputStream());
				String message = socketScanner.nextLine();

				// MESSAGE EXAMPLE : "Hail\n1100\n127.0.0.1"
				if (message.equals("Hail")) {

					int newServentPort = socketScanner.nextInt();
					String newServentIPAddress = socketScanner.next();
					
					System.out.println("GOT -- port: " + newServentPort + ", ip: " + newServentIPAddress + "; from address: " + newNodeSocketAddress);


					PrintWriter socketWriter = new PrintWriter(newServentSocket.getOutputStream());
					
					if (activeServentsInfo.size() == 0) {

						socketWriter.write(-1 + "\n");
						activeServentsInfo.add(new BootstrapServentInfo(newServentIPAddress, newServentPort));

					} else {

						BootstrapServentInfo randServentInfo = activeServentsInfo.get(rand.nextInt(activeServentsInfo.size()));
						socketWriter.write(
								randServentInfo.getListenerPort() + "\n" +
								randServentInfo.getIpAddress() + "\n"
						);

					}
					
					socketWriter.flush();

				// MESSAGE EXAMPLE: "New\n1100\n127.0.0.1"
				} else if (message.equals("New")) {

					int newServentPort = socketScanner.nextInt();
					String newServentIPAddress = socketScanner.next();
					
					System.out.println("adding " + newServentPort + ", from: " + newServentIPAddress);
					
					activeServentsInfo.add(new BootstrapServentInfo(newServentIPAddress, newServentPort));

				}

				newServentSocket.close();

			} catch (IOException e) {
//				AppConfig.timestampedErrorPrint("Failed to read message");
			}
		}
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			AppConfig.timestampedErrorPrint("Bootstrap started without port argument.");
		}

		
		int bsPort = 0;
		try {
			bsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Bootstrap port not valid: " + args[0]);
			System.exit(0);
		}
		
		AppConfig.timestampedStandardPrint("Bootstrap server started on port: " + bsPort);
		
		BootstrapServer bs = new BootstrapServer();
		bs.doBootstrap(bsPort);
	}
}
