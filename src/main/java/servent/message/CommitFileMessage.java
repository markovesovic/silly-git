package servent.message;

import app.ServentInfo;

import java.util.List;

public class CommitFileMessage extends BasicMessage {

    private final List<String> content;
    private final int version;

    public CommitFileMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String filePath, List<String> content, int version, int chordID) {
        super(MessageType.COMMIT_FILE_MESSAGE, senderServentInfo, receiverServentInfo, filePath, chordID);
        this.content = content;
        this.version = version;
    }

    public List<String> getContent() {
        return this.content;
    }

    public int getVersion() {
        return version;
    }
}
