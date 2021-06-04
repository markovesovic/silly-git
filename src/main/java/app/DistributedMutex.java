package app;

import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DistributedMutex {

    public static final AtomicBoolean gotToken = new AtomicBoolean(false);
    public static final AtomicBoolean wantLock = new AtomicBoolean(false);
    public static final AtomicBoolean localLock = new AtomicBoolean(false);

    // TODO: Implement lock for local thread race

    public static void lock() {

        boolean acquired = false;
        while(!acquired) {
            if(!localLock.get()) {
                acquired = localLock.compareAndSet(false, true);
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


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

        localLock.set(false);
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
            AppConfig.timestampedStandardPrint("Token on this node");
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveToken();
        } else {
            AppConfig.timestampedStandardPrint("Sending token to: " + nextNode);
            TokenMessage tokenMessage = new TokenMessage(AppConfig.myServentInfo, nextNode);
            MessageUtil.sendMessage(tokenMessage);
        }
    }

}
