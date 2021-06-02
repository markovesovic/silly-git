package servent.message;

import app.ServentInfo;

public class TokenMessage extends BasicMessage {

    public TokenMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
        super(MessageType.TOKEN_MESSAGE, senderServentInfo, receiverServentInfo);
    }

}
