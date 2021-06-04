package cli;

import app.AppConfig;
import app.Cancellable;
import app.SuccessorPinger;
import cli.command.*;
import servent.SimpleServentListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	private final List<CLICommand> commandList;
	
	public CLIParser(SimpleServentListener listener, SuccessorPinger pinger) {
		this.commandList = new ArrayList<>();
		
		commandList.add(new InfoCommand());
		commandList.add(new PauseCommand());
		commandList.add(new SuccessorInfo());
		commandList.add(new DHTGetCommand());
		commandList.add(new DHTPutCommand());
		commandList.add(new StopCommand(this, listener, pinger));

		commandList.add(new AddFileCommand());
		commandList.add(new CommitFileCommand());
		commandList.add(new RemoveFileCommand());
		commandList.add(new PullFileCommand());
		commandList.add(new PushFileCommand());

		commandList.add(new TestCommand());
	}
	
	@Override
	public void run() {
		while (!AppConfig.INITIALIZED) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Scanner sc = new Scanner(System.in);
		
		while (working) {
			String commandLine = sc.nextLine();
			
			int spacePos = commandLine.indexOf(" ");
			
			String commandName;
			String commandArgs = null;
			if (spacePos != -1) {
				commandName = commandLine.substring(0, spacePos);
				commandArgs = commandLine.substring(spacePos + 1);
			} else {
				commandName = commandLine;
			}
			
			boolean found = false;
			
			for (CLICommand cliCommand : commandList) {
				if (cliCommand.commandName().equals(commandName)) {
					cliCommand.execute(commandArgs);
					found = true;
					break;
				}
			}
			
			if (!found) {
				AppConfig.timestampedErrorPrint("Unknown command: " + commandName);
			}
		}
		
		sc.close();
	}
	
	@Override
	public void stop() {
		this.working = false;
		
	}
}
