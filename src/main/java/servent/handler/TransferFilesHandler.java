package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransferFilesMessage;

import java.util.List;
import java.util.Map;

public class TransferFilesHandler implements MessageHandler {

    private final Message message;

    public TransferFilesHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.TRANSFER_FILES_MESSAGE) {
                AppConfig.timestampedStandardPrint("Transfer file handler got wrong message type");
                return;
            }
            if(!(this.message instanceof TransferFilesMessage)) {
                AppConfig.timestampedErrorPrint("Transfer file handler got wrong message instance");
                return;
            }

            Map<String, Map<Integer, List<String>>> newFiles = ((TransferFilesMessage) message).getValues();
            AppConfig.timestampedStandardPrint("Got new files: " + newFiles.toString());
            newFiles.forEach((key, value) -> AppConfig.chordState.getWarehouseFiles().put(key, value));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
