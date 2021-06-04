package servent.message;

import app.ServentInfo;

public class RemoveNodeMessage extends BasicMessage {

    private final ServentInfo targetInfo;
    private final boolean gotMutex;

    public RemoveNodeMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, ServentInfo targetInfo, boolean gotMutex) {
        super(MessageType.REMOVE_NODE_MESSAGE, senderServentInfo, receiverServentInfo);
        this.targetInfo = targetInfo;
        this.gotMutex = gotMutex;
    }

    public ServentInfo getTargetInfo() {
        return targetInfo;
    }

    public boolean isGotMutex() {
        return gotMutex;
    }
}
