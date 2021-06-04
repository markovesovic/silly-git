package servent.handler;

import app.AppConfig;
import app.DistributedMutex;
import app.ServentInfo;
import app.SuccessorPinger;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveNodeMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class RemoveNodeHandler implements MessageHandler{

    private final Message message;

    public RemoveNodeHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.REMOVE_NODE_MESSAGE) {
                AppConfig.timestampedStandardPrint("Remove node handler got wrong message type");
                return;
            }
            if(!(this.message instanceof RemoveNodeMessage)) {
                AppConfig.timestampedStandardPrint("Remove node handler got wrong message instance");
                return;
            }

            if(this.message.getSenderServentInfo().getIpAddress().equals(AppConfig.myServentInfo.getIpAddress()) &&
                    this.message.getSenderServentInfo().getListenerPort() == AppConfig.myServentInfo.getListenerPort()) {

                List<ServentInfo> nodesToRemove = new ArrayList<>();
                nodesToRemove.add(((RemoveNodeMessage) this.message).getTargetInfo());
                AppConfig.chordState.removeNodes(nodesToRemove);

                if(!((RemoveNodeMessage) this.message).isGotMutex()) {
                    DistributedMutex.receiveToken();
                }

                synchronized (SuccessorPinger.waitLock) {
                    SuccessorPinger.waitLock.notify();
                }
                return;
            }

            List<ServentInfo> nodesToRemove = new ArrayList<>();
            nodesToRemove.add(((RemoveNodeMessage) this.message).getTargetInfo());
            AppConfig.chordState.removeNodes(nodesToRemove);

            RemoveNodeMessage removeNodeMessage = new RemoveNodeMessage(
                    this.message.getSenderServentInfo(),
                    AppConfig.chordState.getPredecessor(),
                    ((RemoveNodeMessage) message).getTargetInfo(),
                    DistributedMutex.gotToken.get()
            );
            MessageUtil.sendMessage(removeNodeMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
