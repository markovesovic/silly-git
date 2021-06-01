package cli.command;

import app.AppConfig;
import app.ChordState;

public class DHTPutCommand implements CLICommand {

	@Override
	public String commandName() {
		return "dht_put";
	}

	@Override
	public void execute(String args) {
		String[] splitArgs = args.split(" ");
		
		if (splitArgs.length == 2) {
			int key;
			int value;
			try {
				key = Integer.parseInt(splitArgs[0]);
				value = Integer.parseInt(splitArgs[1]);
				
				if (key < 0 || key >= ChordState.CHORD_SIZE) {
					throw new NumberFormatException();
				}
				if (value < 0) {
					throw new NumberFormatException();
				}
				
				AppConfig.chordState.putValue(key, value);
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Invalid key and value pair. Both should be ints. 0 <= key <= " + ChordState.CHORD_SIZE
						+ ". 0 <= value.");
			}
		} else {
			AppConfig.timestampedErrorPrint("Invalid arguments for put");
		}

	}

}
