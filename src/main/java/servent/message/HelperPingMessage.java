package servent.message;

import app.ServentInfo;

public class HelperPingMessage extends BasicMessage {

    private final ServentInfo targetToPing;

    public HelperPingMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, ServentInfo targetToPing) {
        super(MessageType.HELPER_PING_MESSAGE, senderServentInfo, receiverServentInfo);
        this.targetToPing = targetToPing;
    }

    public ServentInfo getTargetToPing() {
        return targetToPing;
    }
}
