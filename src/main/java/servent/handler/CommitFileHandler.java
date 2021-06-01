package servent.handler;

import app.AppConfig;
import servent.message.CommitFileMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.List;

public class CommitFileHandler implements MessageHandler {

    private final Message message;

    public CommitFileHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(message.getMessageType() != MessageType.COMMIT_FILE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Commit file handler got wrong message type");
                return;
            }
            if(!(message instanceof CommitFileMessage)) {
                AppConfig.timestampedStandardPrint("Add file handler got wrong message instance");
                return;
            }

            List<String> content = ((CommitFileMessage)message).getContent();

            AppConfig.chordState.commitFile(message.getMessageText(), content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
