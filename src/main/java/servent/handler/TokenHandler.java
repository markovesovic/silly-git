package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import servent.message.Message;
import servent.message.MessageType;

public class TokenHandler implements MessageHandler {

    private final Message message;

    public TokenHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.TOKEN_MESSAGE) {
                AppConfig.timestampedErrorPrint("Token handler got wrong message type");
            }
            DistributedMutex.receiveToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
