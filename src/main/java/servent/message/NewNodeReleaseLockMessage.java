package servent.message;

import app.ServentInfo;

public class NewNodeReleaseLockMessage extends BasicMessage {

    public NewNodeReleaseLockMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
        super(MessageType.NEW_NODE_RELEASE_LOCK_MESSAGE, senderServentInfo, receiverServentInfo);
    }

}
