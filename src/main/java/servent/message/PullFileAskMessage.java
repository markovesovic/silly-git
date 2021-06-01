package servent.message;

import app.ServentInfo;

public class PullFileAskMessage extends BasicMessage {

    public PullFileAskMessage(ServentInfo myServentInfo, ServentInfo nextNode, String filePath, int version) {
        super(MessageType.PULL_FILE_ASK_MESSAGE, myServentInfo, nextNode, filePath + ":" + version);
    }
}
