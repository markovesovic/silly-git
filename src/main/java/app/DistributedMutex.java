package app;

import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DistributedMutex {

    public static final AtomicBoolean gotToken = new AtomicBoolean(false);
    public static final AtomicBoolean wantLock = new AtomicBoolean(false);

    public static void lock() {
        wantLock.set(true);

        while(!gotToken.get()) {
            try {
                AppConfig.timestampedStandardPrint("Waiting for lock in while loop");
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unlock() {
        gotToken.set(false);
        wantLock.set(false);
        sendTokenForward();
    }

    public static void receiveToken() {
        if(wantLock.get()) {
            gotToken.set(true);
        } else {
            sendTokenForward();
        }
    }

    public static void sendTokenForward() {
        ServentInfo nextNode = AppConfig.chordState.getNextNodeServentInfo();
        if(nextNode == null) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveToken();
        } else {
            TokenMessage tokenMessage = new TokenMessage(AppConfig.myServentInfo, nextNode);
            MessageUtil.sendMessage(tokenMessage);
        }
    }

}
