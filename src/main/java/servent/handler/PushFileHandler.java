package servent.handler;

import app.AppConfig;
import servent.message.CommitFileMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PushFileMessage;

import java.util.List;

public class PushFileHandler implements MessageHandler {

    private final Message message;

    public PushFileHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.PUSH_FILE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Push file handler got wrong message type");
                return;
            }
            if(!(this.message instanceof PushFileMessage)) {
                AppConfig.timestampedStandardPrint("Push file handler got wrong message instance");
                return;
            }
            List<String> content = ((PushFileMessage) message).getContent();

            AppConfig.chordState.pushFile(message.getMessageText(), content, message.getChordID());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
