package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class SorryHandler implements MessageHandler {

	private final Message clientMessage;
	
	public SorryHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		if (clientMessage.getMessageType() == MessageType.SORRY) {
			AppConfig.timestampedStandardPrint("Couldn't enter Chord system because of collision. Change my listener port, please.");
			System.exit(0);
		}

		AppConfig.timestampedErrorPrint("Sorry handler got a message that is not SORRY");

	}

}
