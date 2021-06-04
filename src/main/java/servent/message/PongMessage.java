package servent.message;

import app.ServentInfo;

public class PongMessage extends BasicMessage {

    public PongMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
        super(MessageType.PONG_MESSAGE, senderServentInfo, receiverServentInfo);
    }

}
