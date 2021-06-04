package servent.handler;


import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PongMessage;
import servent.message.util.MessageUtil;

public class PingHandler implements MessageHandler {

    private final Message message;

    public PingHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.PING_MESSAGE) {
                AppConfig.timestampedErrorPrint("Ping handler got wrong message type");
                return;
            }

            PongMessage pongMessage = new PongMessage(AppConfig.myServentInfo, this.message.getSenderServentInfo());
            MessageUtil.sendMessage(pongMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
