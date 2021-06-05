package cli.command;

import app.AppConfig;
import app.DistributedMutex;
import app.SuccessorPinger;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.ExitMessage;
import servent.message.TransferFilesMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StopCommand implements CLICommand {

	private final CLIParser parser;
	private final SimpleServentListener listener;
	private final SuccessorPinger pinger;

	public StopCommand(CLIParser parser, SimpleServentListener listener, SuccessorPinger pinger) {
		this.parser = parser;
		this.listener = listener;
		this.pinger = pinger;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		try {
			AppConfig.timestampedStandardPrint("Stopping...");
			parser.stop();
			pinger.stop();

			// Lock whole system while you are reconstructing it
			DistributedMutex.lock();

			try {
				Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_HOST, AppConfig.BOOTSTRAP_PORT);

				PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
				bsWriter.write(
						"Exit\n" +
								AppConfig.myServentInfo.getListenerPort() + "\n" +
								AppConfig.myServentInfo.getIpAddress() + "\n"
				);
				bsWriter.flush();
				bsSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			AppConfig.timestampedStandardPrint("My next node: " + AppConfig.chordState.getNextNodeServentInfo() + ", all nodes list size: " + AppConfig.chordState.getAllNodeInfo().size());
			if (AppConfig.chordState.getAllNodeInfo().size() == 0) {
				AppConfig.timestampedErrorPrint("Last node in system is exiting. System shutting down...");
				System.exit(0);
			}

			TransferFilesMessage transferFilesMessage = new TransferFilesMessage(
					AppConfig.myServentInfo,
					AppConfig.chordState.getNextNodeServentInfo(),
					AppConfig.chordState.getWarehouseFiles()
			);
			MessageUtil.sendMessage(transferFilesMessage);

			ExitMessage exitMessage = new ExitMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNodeServentInfo());
			MessageUtil.sendMessage(exitMessage);

			// Stop listener only when all nodes are informed of change
//		listener.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
