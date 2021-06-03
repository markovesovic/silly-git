package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import servent.SimpleServentListener;
import servent.message.ExitMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class ExitHandler implements MessageHandler {

    private final Message message;
    private final SimpleServentListener listener;

    public ExitHandler(Message message, SimpleServentListener listener) {
        this.message = message;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.EXIT_MESSAGE) {
                AppConfig.timestampedErrorPrint("Exit handler got wrong message type");
                return;
            }
            if(!message.getSenderServentInfo().getIpAddress().equals(AppConfig.myServentInfo.getIpAddress()) ||
                    message.getSenderServentInfo().getListenerPort() != AppConfig.myServentInfo.getListenerPort()) {

                ServentInfo oldNodeInfo = message.getSenderServentInfo();

                List<ServentInfo> oldNodes = new ArrayList<>();
                oldNodes.add(oldNodeInfo);

                ServentInfo nextNodeInfo = null;
                if(AppConfig.chordState.getNextNodeServentInfo().getIpAddress().equals(message.getSenderServentInfo().getIpAddress()) &&
                        AppConfig.chordState.getNextNodeServentInfo().getListenerPort() == message.getSenderServentInfo().getListenerPort()) {

                    nextNodeInfo = AppConfig.chordState.getNextNodeServentInfo();
                }

                AppConfig.chordState.removeNodes(oldNodes);
                if(nextNodeInfo == null) {
                    nextNodeInfo = AppConfig.chordState.getNextNodeServentInfo();
                }

                Message nextExitMessage = new ExitMessage(message.getSenderServentInfo(), nextNodeInfo);
                MessageUtil.sendMessage(nextExitMessage);

                return;
            }

            // Gracefully exit
            DistributedMutex.unlock();
            listener.stop();
            // Sleep - wait for message to be sent
            AppConfig.timestampedStandardPrint("Before sleep");
            Thread.sleep(5000);
            AppConfig.timestampedStandardPrint("Waking up after sleep");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
