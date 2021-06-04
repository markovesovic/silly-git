package servent.message.util;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DelayedMessageSender implements Runnable {

	private final Message messageToSend;
	
	public DelayedMessageSender(Message messageToSend) {
		this.messageToSend = messageToSend;
	}

	@Override
	public void run() {

		try {
			Thread.sleep((long)(Math.random() * 1000) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (MessageUtil.MESSAGE_UTIL_PRINTING && messageToSend.getMessageType() != MessageType.TOKEN_MESSAGE
				&& messageToSend.getMessageType() != MessageType.PING_MESSAGE && messageToSend.getMessageType() != MessageType.PONG_MESSAGE) {
			AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
		}
		
		try {
			Socket sendSocket = new Socket(messageToSend.getReceiverServentInfo().getIpAddress(), messageToSend.getReceiverServentInfo().getListenerPort());
			
			ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(messageToSend);
			oos.flush();
			
			sendSocket.close();
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
		}
	}
	
}
