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

            String[] args = message.getMessageText().split(":");
            String filePath = args[0];
            int version = Integer.parseInt(args[1]);

            int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % ChordState.CHORD_SIZE;

            if( AppConfig.chordState.isKeyMine(filePathHash) ) {

                PullFileResponse pullFileResponse = null;

                if( AppConfig.chordState.getCurrentNewestVersion().get(filePath) != null ) {

                    version = version == -1 ? AppConfig.chordState.getCurrentNewestVersion().get(filePath) : version;

                    List<String> content = Files.readAllLines(Paths.get(AppConfig.WAREHOUSE_PATH + filePath + "_" + version));

                    pullFileResponse = new PullFileResponse(filePath, content);
                }

                PullFileTellMessage pullFileTellMessage = new PullFileTellMessage(AppConfig.myServentInfo, message.getSenderServentInfo(), filePath, pullFileResponse);
                MessageUtil.sendMessage(pullFileTellMessage);

                return;
            }

            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(filePathHash);
            PullFileAskMessage pullFileAskMessage = new PullFileAskMessage(message.getSenderServentInfo(), nextNode, filePath, version);
            MessageUtil.sendMessage(pullFileAskMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
