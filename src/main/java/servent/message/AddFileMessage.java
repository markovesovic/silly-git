package servent.message;

import app.ServentInfo;

import java.io.File;
import java.util.List;

public class AddFileMessage extends BasicMessage {

    private final List<String> content;

    public AddFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String filePath, List<String> content) {
        super(MessageType.ADD_FILE_MESSAGE, senderServentInfo, receiverServentInfo, filePath);
        this.content = content;
    }

    public List<String> getContent() {
        return this.content;
    }
}
