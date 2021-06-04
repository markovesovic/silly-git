package servent.handler;

import app.AppConfig;
import servent.message.HelperPingMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PingMessage;
import servent.message.util.MessageUtil;

public class HelperPingHandler implements MessageHandler {

    private final Message message;

    public HelperPingHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.HELPER_PING_MESSAGE) {
                AppConfig.timestampedErrorPrint("Helper ping handler got wrong message type");
                return;
            }
            if(!(this.message instanceof HelperPingMessage)) {
                AppConfig.timestampedStandardPrint("Helper ping handler got wrong message instance");
                return;
            }

            PingMessage pingMessage = new PingMessage(AppConfig.myServentInfo, ((HelperPingMessage) message).getTargetToPing());
            MessageUtil.sendMessage(pingMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
