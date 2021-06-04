package servent.message;

import app.ServentInfo;

import java.util.List;

public class PushFileMessage extends BasicMessage {

    private final List<String> content;

    public PushFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText, List<String> content, int chordID) {
        super(MessageType.PUSH_FILE_MESSAGE, senderServentInfo, receiverServentInfo, messageText, chordID);
        this.content = content;
    }

    public List<String> getContent() {
        return content;
    }
}
