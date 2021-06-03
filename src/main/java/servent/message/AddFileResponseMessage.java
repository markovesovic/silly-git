package servent.message;

import app.ServentInfo;

public class AddFileResponseMessage extends BasicMessage {

    public AddFileResponseMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String text, int chordID) {
        super(MessageType.ADD_FILE_RESPONSE_MESSAGE, senderServentInfo, receiverServentInfo, text, chordID);
    }

}
