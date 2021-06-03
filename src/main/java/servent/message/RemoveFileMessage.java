package servent.message;

import app.ServentInfo;

public class RemoveFileMessage extends BasicMessage {

    public RemoveFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText, int chordID) {
        super(MessageType.REMOVE_FILE_MESSAGE, senderServentInfo, receiverServentInfo, messageText, chordID);
    }

}
