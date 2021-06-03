package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.AddFileResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AddFileResponseHandler implements MessageHandler {

    private final Message message;

    public AddFileResponseHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.ADD_FILE_RESPONSE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Handler got wrong type");
                return;
            }
            if(!(this.message instanceof AddFileResponseMessage)) {
                AppConfig.timestampedStandardPrint("Wrong message instance");
                return;
            }
            int chordID = this.message.getChordID();
            if( !AppConfig.chordState.isKeyMine( chordID ) ) {

                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordID);
                AddFileResponseMessage addFileResponseMessage = new AddFileResponseMessage(
                        message.getSenderServentInfo(),
                        nextNode,
                        message.getMessageText(),
                        chordID
                );
                MessageUtil.sendMessage(addFileResponseMessage);
                return;
            }

            // I got response for my add message
            AppConfig.timestampedStandardPrint(this.message.getMessageText());
            DistributedMutex.unlock();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
