package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.CommitFileResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.io.FileWriter;

public class CommitFileResponseHandler implements MessageHandler {

    private final Message message;

    public CommitFileResponseHandler(final Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.COMMIT_FILE_RESPONSE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Commit file handler got wrong message type");
                return;
            }

            if(!(this.message instanceof CommitFileResponseMessage)) {
                AppConfig.timestampedStandardPrint("Commit file handler got wrong message instance");
                return;
            }

            int chordID = this.message.getChordID();
            if( !AppConfig.chordState.isKeyMine( chordID ) ) {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordID);
                CommitFileResponseMessage commitFileResponseMessage = new CommitFileResponseMessage(
                        message.getSenderServentInfo(),
                        nextNode,
                        message.getMessageText(),
                        chordID,
                        ((CommitFileResponseMessage) message).isConflict(),
                        ((CommitFileResponseMessage) message).getContent(),
                        ((CommitFileResponseMessage) message).getFilePath()
                );
                MessageUtil.sendMessage(commitFileResponseMessage);
                return;
            }

            // I got response for my own message - do conflict handling logic
            AppConfig.timestampedStandardPrint("Node responded: " + this.message.getMessageText());

            if(!((CommitFileResponseMessage) message).isConflict()) {
                int lastVersion = AppConfig.chordState.getCurrentFileVersionsInWorkingDir().get(((CommitFileResponseMessage) message).getFilePath());
                AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(((CommitFileResponseMessage) message).getFilePath(), lastVersion + 1);
                return;
            }
            FileWriter myWriter = new FileWriter(AppConfig.ROOT_PATH + ((CommitFileResponseMessage) message).getFilePath() + "__temp");

            for (String s : ((CommitFileResponseMessage) message).getContent()) {
                myWriter.write(s + System.lineSeparator());
            }

            myWriter.flush();
            myWriter.close();


            AppConfig.timestampedStandardPrint("Conflict happened! Temp file is in the same dir as your local copy");
            AppConfig.timestampedStandardPrint("- Use push (commit --force) to overwrite remote with your local copy");
            AppConfig.timestampedStandardPrint("- Use pull to overwrite your local copy with remote version");

            DistributedMutex.unlock();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
