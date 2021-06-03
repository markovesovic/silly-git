package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class RemoveFileHandler implements MessageHandler {

    private final Message message;

    public RemoveFileHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.REMOVE_FILE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Remove handler got wrong message type");
            }

            AppConfig.chordState.removeFile(message.getMessageText(), message.getChordID());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
