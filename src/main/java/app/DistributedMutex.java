package app;

import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DistributedMutex {

    public static final AtomicBoolean gotToken = new AtomicBoolean(false);
    public static final AtomicBoolean wantLock = new AtomicBoolean(false);
    public static final AtomicBoolean localLock = new AtomicBoolean(false);
    private static int print = 0;

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

        AppConfig.timestampedStandardPrint("Waiting for distributed token");
        while(!gotToken.get()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        AppConfig.timestampedStandardPrint("Got distributed token");
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
            if(print++ > 5) {
                AppConfig.timestampedStandardPrint("Token on this node");
                print = 0;
            }
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveToken();
        } else {
            if(print++ > 5) {
                AppConfig.timestampedStandardPrint("Sending token to: " + nextNode);
                print = 0;
            }
            TokenMessage tokenMessage = new TokenMessage(AppConfig.myServentInfo, nextNode);
            MessageUtil.sendMessage(tokenMessage);
        }
    }

}
