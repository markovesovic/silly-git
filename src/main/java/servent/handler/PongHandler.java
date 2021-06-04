package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PongMessage;
import servent.message.util.MessageUtil;

public class PongHandler implements MessageHandler {

    private final Message message;

    public PongHandler(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(this.message.getMessageType() != MessageType.PONG_MESSAGE) {
                AppConfig.timestampedStandardPrint("Pong handler got wrong message type");
                return;
            }

            if(this.message.getSenderServentInfo().getListenerPort() == AppConfig.chordState.getNextNodeServentInfo().getListenerPort()
                    && this.message.getSenderServentInfo().getIpAddress().equals(AppConfig.chordState.getNextNodeServentInfo().getIpAddress())) {

                AppConfig.timeAtLastPong.set(System.currentTimeMillis());
                return;
            }
            PongMessage pongMessage = new PongMessage(this.message.getSenderServentInfo(), AppConfig.chordState.getNextNodeServentInfo());
            MessageUtil.sendMessage(pongMessage);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
