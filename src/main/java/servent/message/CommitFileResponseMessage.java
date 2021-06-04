package servent.message;

import app.ServentInfo;

import java.util.List;

public class CommitFileResponseMessage extends BasicMessage {

    private final List<String> content;
    private final boolean conflict;
    private final String filePath;

    public CommitFileResponseMessage(ServentInfo senderServentInfo,
                                     ServentInfo receiverServentInfo,
                                     String messageText,
                                     int chordID,
                                     boolean conflict,
                                     List<String> content,
                                     String filePath) {
        super(MessageType.COMMIT_FILE_RESPONSE_MESSAGE, senderServentInfo, receiverServentInfo, messageText, chordID);
        this.content = content;
        this.conflict = conflict;
        this.filePath = filePath;
    }

    public List<String> getContent() {
        return content;
    }

    public boolean isConflict() {
        return conflict;
    }

    public String getFilePath() {
        return filePath;
    }
}
