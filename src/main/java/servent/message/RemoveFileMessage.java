package servent.message;

import app.ServentInfo;

public class RemoveFileMessage extends BasicMessage {

    public RemoveFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText) {
        super(MessageType.REMOVE_FILE_MESSAGE, senderServentInfo, receiverServentInfo, messageText);
    }

}
