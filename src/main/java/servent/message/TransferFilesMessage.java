package servent.message;

import app.ServentInfo;

import java.util.List;
import java.util.Map;

public class TransferFilesMessage extends BasicMessage {

    private final Map<String, Map<Integer, List<String>>> values;

    public TransferFilesMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, Map<String, Map<Integer, List<String>>> values) {
        super(MessageType.TRANSFER_FILES_MESSAGE, senderServentInfo, receiverServentInfo);
        this.values = values;
    }

    public Map<String, Map<Integer, List<String>>> getValues() {
        return values;
    }
}
