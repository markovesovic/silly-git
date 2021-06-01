package servent.message;

import app.ServentInfo;

public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	public NewNodeMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
		super(MessageType.NEW_NODE, senderServentInfo, receiverServentInfo);
	}
}
