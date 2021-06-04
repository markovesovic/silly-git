package servent.message;

import app.ServentInfo;

public class PingMessage extends BasicMessage {

    public PingMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
        super(MessageType.PING_MESSAGE, senderServentInfo, receiverServentInfo);
    }

}
