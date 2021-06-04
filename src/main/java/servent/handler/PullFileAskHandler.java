package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PullFileAskMessage;
import servent.message.PullFileTellMessage;
import servent.message.util.MessageUtil;
import servent.response.PullFileResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PullFileAskHandler implements MessageHandler {

    private final Message message;

    public PullFileAskHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(message.getMessageType() != MessageType.PULL_FILE_ASK_MESSAGE) {
                AppConfig.timestampedErrorPrint("Pull file handler got wrong message type");
                return;
            }
            String filePath = message.getMessageText().split(":")[0];
            int version = Integer.parseInt(message.getMessageText().split(":")[1]);

            PullFileResponse pullFileResponse = AppConfig.chordState.pullFile(filePath, version, message.getChordID());

            if(pullFileResponse.getVersion() != -2) {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(message.getChordID());
                PullFileTellMessage pullFileTellMessage = new PullFileTellMessage(AppConfig.myServentInfo, nextNode, "", message.getChordID(), pullFileResponse);
                MessageUtil.sendMessage(pullFileTellMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
