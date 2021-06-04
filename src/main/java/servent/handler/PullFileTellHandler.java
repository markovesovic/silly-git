package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PullFileTellMessage;
import servent.message.util.MessageUtil;
import servent.response.PullFileResponse;

import java.io.File;
import java.io.FileWriter;

public class PullFileTellHandler implements MessageHandler {

    private final Message message;

    public PullFileTellHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.PULL_FILE_TELL_MESSAGE) {
                AppConfig.timestampedErrorPrint("Pull file handler got wrong message type");
                return;
            }

            if(!(this.message instanceof PullFileTellMessage)) {
                AppConfig.timestampedErrorPrint("Pull file handler got wrong message instance");
                return;
            }

            int chordID = this.message.getChordID();
            if( !AppConfig.chordState.isKeyMine( chordID ) ) {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordID);
                PullFileTellMessage pullFileTellMessage = new PullFileTellMessage(
                        message.getSenderServentInfo(),
                        nextNode,
                        "",
                        chordID,
                        ((PullFileTellMessage) this.message).getResponse()
                );
                MessageUtil.sendMessage(pullFileTellMessage);
                return;
            }


            // I got response for my pull ask message
            PullFileResponse response = ((PullFileTellMessage) message).getResponse();

            if(response.getVersion() == -1) {
                AppConfig.timestampedStandardPrint("Node responded: " + response.getFilePath());
                return;
            }

            AppConfig.timestampedStandardPrint("File successfully retrieved from remote!");
            AppConfig.chordState.saveFileToFs(response.getFilePath(), response.getContent());
            AppConfig.chordState.getCurrentFileVersionsInWorkingDir().put(response.getFilePath(), response.getVersion());

            DistributedMutex.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
