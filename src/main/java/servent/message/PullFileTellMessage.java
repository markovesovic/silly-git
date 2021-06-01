package servent.message;

import app.ServentInfo;
import servent.response.PullFileResponse;

public class PullFileTellMessage extends BasicMessage {

    private final PullFileResponse response;


    public PullFileTellMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String text, PullFileResponse response) {
        super(MessageType.PULL_FILE_TELL_MESSAGE, senderServentInfo, receiverServentInfo, text);
        this.response = response;
    }

    public PullFileResponse getResponse() {
        return response;
    }
}
