package servent.handler;

import app.AppConfig;
import app.ChordState;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler {

	private final Message clientMessage;
	
	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		try {
			if (clientMessage.getMessageType() != MessageType.WELCOME) {
				AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
				return;
			}

			WelcomeMessage welcomeMsg = (WelcomeMessage) clientMessage;

			AppConfig.chordState.init(welcomeMsg, clientMessage.getSenderServentInfo());

			UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNodeServentInfo(), "");
			MessageUtil.sendMessage(um);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
