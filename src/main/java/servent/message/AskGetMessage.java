package servent.message;

import app.ServentInfo;

public class AskGetMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;

	public AskGetMessage(ServentInfo senderServentInfo, ServentInfo receiverSenderInfo, String text) {
		super(MessageType.ASK_GET, senderServentInfo, receiverSenderInfo, text);
	}
}
