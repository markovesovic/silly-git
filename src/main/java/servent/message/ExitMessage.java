package servent.message;

import app.ServentInfo;

public class ExitMessage extends BasicMessage {

    public ExitMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
        super(MessageType.EXIT_MESSAGE, senderServentInfo, receiverServentInfo);
    }

}
