package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveFileResponseMessage;
import servent.message.util.MessageUtil;

public class RemoveFileResponseHandler implements MessageHandler {

    private final Message message;

    public RemoveFileResponseHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.REMOVE_FILE_RESPONSE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Remove file response handler got wrong message type");
                return;
            }

            int chordID = this.message.getChordID();
            if( !AppConfig.chordState.isKeyMine( chordID) ) {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordID);
                RemoveFileResponseMessage removeFileResponseMessage = new RemoveFileResponseMessage(
                        message.getSenderServentInfo(),
                        nextNode,
                        message.getMessageText(),
                        chordID
                );
                MessageUtil.sendMessage(removeFileResponseMessage);
                return;
            }

            AppConfig.timestampedStandardPrint(this.message.getMessageText());
            DistributedMutex.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
