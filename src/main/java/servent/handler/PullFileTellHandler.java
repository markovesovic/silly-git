package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PullFileTellMessage;
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

            PullFileResponse response = ((PullFileTellMessage)message).getResponse();

            if(response == null) {
                AppConfig.timestampedStandardPrint("There is no such file in system: " + message.getMessageText());
                return;
            }

            File file = new File(AppConfig.ROOT_PATH + response.getFilePath());

            file.delete();
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            for(String str : response.getContent()) {
                writer.write(str + System.lineSeparator());
            }

            writer.flush();
            writer.close();

            AppConfig.timestampedStandardPrint("File: " + response.getFilePath() + " successfully pulled");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
