package cli.command;

import app.AppConfig;
import app.DistributedMutex;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.ExitMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {

	private final CLIParser parser;
	private final SimpleServentListener listener;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener) {
		this.parser = parser;
		this.listener = listener;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();

		// Lock whole system while you are reconstructing it
		DistributedMutex.lock();

		ExitMessage exitMessage = new ExitMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNodeServentInfo());
		MessageUtil.sendMessage(exitMessage);

		// Stop listener only when all nodes are informed of change
//		listener.stop();
	}

}
