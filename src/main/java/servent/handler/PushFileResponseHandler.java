package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PushFileResponseMessage;
import servent.message.util.MessageUtil;

public class PushFileResponseHandler implements MessageHandler {

    private final Message message;

    public PushFileResponseHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.PUSH_FILE_RESPONSE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Push file response handler got wrong message type");
                return;
            }

            if(!(this.message instanceof PushFileResponseMessage)) {
                AppConfig.timestampedStandardPrint("Push file response handler got wrong message type");
                return;
            }

            int chordID = this.message.getChordID();
            if( !AppConfig.chordState.isKeyMine( chordID ) ) {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordID);
                PushFileResponseMessage pushFileResponseMessage = new PushFileResponseMessage(
                        message.getSenderServentInfo(),
                        nextNode,
                        message.getMessageText(),
                        message.getChordID(),
                        ((PushFileResponseMessage) message).getFilePath(),
                        ((PushFileResponseMessage) message).getUpToDateVersion()
                );
                MessageUtil.sendMessage(pushFileResponseMessage);
                return;
            }


            AppConfig.timestampedStandardPrint("Node responded: " + this.message.getMessageText());
            int version = ((PushFileResponseMessage) message).getUpToDateVersion();
            if(version == 0) {
                return;
            }
            AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(((PushFileResponseMessage) message).getFilePath(), version);

            DistributedMutex.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
