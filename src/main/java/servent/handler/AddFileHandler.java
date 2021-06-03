package servent.handler;

import app.AppConfig;
import servent.message.AddFileMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.io.File;
import java.util.List;

public class AddFileHandler implements MessageHandler {

    private final Message message;

    public AddFileHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(message.getMessageType() != MessageType.ADD_FILE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Add file handler got wrong message type");
                return;
            }
            if(!(message instanceof AddFileMessage)) {
                AppConfig.timestampedStandardPrint("Add file handler got wrong message instance");
                return;
            }

            List<String> content = ((AddFileMessage)message).getContent();

            AppConfig.chordState.addFile(message.getMessageText(), content, message.getChordID());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
