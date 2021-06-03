package servent.message;

import app.ServentInfo;

public class RemoveFileResponseMessage extends BasicMessage {

    public RemoveFileResponseMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText, int chordID) {
        super(MessageType.REMOVE_FILE_RESPONSE_MESSAGE, senderServentInfo, receiverServentInfo, messageText, chordID);
    }

}
