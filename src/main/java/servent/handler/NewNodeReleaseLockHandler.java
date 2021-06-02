package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import servent.message.Message;
import servent.message.MessageType;

public class NewNodeReleaseLockHandler implements MessageHandler {

    private final Message message;

    public NewNodeReleaseLockHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.NEW_NODE_RELEASE_LOCK_MESSAGE) {
                AppConfig.timestampedStandardPrint("New node release handler got wrong message type");
                return;
            }

            DistributedMutex.unlock();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
