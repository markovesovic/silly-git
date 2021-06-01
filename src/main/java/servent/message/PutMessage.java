package servent.message;

import app.ServentInfo;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, int key, int value) {
		super(MessageType.PUT, senderServentInfo, receiverServentInfo, key + ":" + value);
	}
}
