package servent.message;

import app.ServentInfo;

import java.util.List;

public class CommitFileMessage extends BasicMessage {

    private final List<String> content;

    public CommitFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String filePath, List<String> content) {
        super(MessageType.COMMIT_FILE_MESSAGE, senderServentInfo, receiverServentInfo, filePath);
        this.content = content;
    }

    public List<String> getContent() {
        return this.content;
    }
}
