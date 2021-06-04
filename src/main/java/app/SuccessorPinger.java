package app;

import servent.message.HelperPingMessage;
import servent.message.PingMessage;
import servent.message.RemoveNodeMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SuccessorPinger implements Runnable, Cancellable {

    private boolean working = true;
    public static final Object waitLock = new Object();

    @Override
    public void run() {

        while(!AppConfig.INITIALIZED) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(working) {

            if(AppConfig.chordState.getNextNodeServentInfo() != null) {
                PingMessage pingMessage = new PingMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNodeServentInfo());
                MessageUtil.sendMessage(pingMessage);
            }
            long timeSinceLastPong = System.currentTimeMillis() - AppConfig.timeAtLastPong.get();

            if(timeSinceLastPong > AppConfig.WEAK_FAILURE_LIMIT) {
                HelperPingMessage helperPingMessage = new HelperPingMessage(
                        AppConfig.myServentInfo,
                        AppConfig.chordState.getPredecessor(),
                        AppConfig.chordState.getNextNodeServentInfo());
                MessageUtil.sendMessage(helperPingMessage);
            }

            AppConfig.timestampedStandardPrint("Time since last pong response: " + timeSinceLastPong);

            if(timeSinceLastPong > AppConfig.STRONG_FAILURE_LIMIT) {

                try {
                    Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_HOST, AppConfig.BOOTSTRAP_PORT);

                    PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
                    bsWriter.write(
                            "Exit\n" +
                                    AppConfig.chordState.getNextNodeServentInfo().getListenerPort() + "\n" +
                                    AppConfig.chordState.getNextNodeServentInfo().getIpAddress() + "\n"
                    );
                    bsWriter.flush();
                    bsSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AppConfig.timestampedErrorPrint("My successor should be removed");
                RemoveNodeMessage removeNodeMessage = new RemoveNodeMessage(
                        AppConfig.myServentInfo,
                        AppConfig.chordState.getPredecessor(),
                        AppConfig.chordState.getNextNodeServentInfo(),
                        false
                );
                MessageUtil.sendMessage(removeNodeMessage);

                synchronized (waitLock) {
                    try {
                        waitLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AppConfig.timeAtLastPong.set(System.currentTimeMillis());
            }


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void stop() {
        this.working = false;
    }
}
