package app;

import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServentInitializer implements Runnable {

	private ServentInfo getSomeServentInfo() {
		ServentInfo nodeServentInfo = null;

		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_HOST, AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			// "Hail\n1100\n127.0.0.1
			bsWriter.write(
					"Hail\n" +
						AppConfig.myServentInfo.getListenerPort() + "\n" +
						AppConfig.myServentInfo.getIpAddress() + "\n"
			);
			bsWriter.flush();
			
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());

			int port = bsScanner.nextInt();

			if(port == -1) {
				nodeServentInfo = new ServentInfo("doesNotMatter", port);
			} else {
				String IPAddress = bsScanner.next();
				nodeServentInfo = new ServentInfo(IPAddress, port);
			}


			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return nodeServentInfo;
	}
	
	@Override
	public void run() {
		ServentInfo someServentInfo = getSomeServentInfo();


		if (someServentInfo == null) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}

		if (someServentInfo.getListenerPort() == -1) { //bootstrap gave us -1 -> we are first
			AppConfig.timestampedStandardPrint("First node in Chord system.");
			DistributedMutex.receiveToken();
			return;
		}

		NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo, someServentInfo);
		MessageUtil.sendMessage(nnm);

	}

}
