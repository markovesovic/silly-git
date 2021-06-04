package servent.message;

import app.ServentInfo;

public class PushFileResponseMessage extends BasicMessage {

    private final int upToDateVersion;
    private final String filePath;

    public PushFileResponseMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText, int chordID, String filePath, int upToDateVersion) {
        super(MessageType.PUSH_FILE_RESPONSE_MESSAGE, senderServentInfo, receiverServentInfo, messageText, chordID);
        this.upToDateVersion = upToDateVersion;
        this.filePath = filePath;
    }

    public int getUpToDateVersion() {
        return upToDateVersion;
    }

    public String getFilePath() {
        return filePath;
    }
}
