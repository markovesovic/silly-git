package servent.message;

import app.ServentInfo;

import java.io.Serializable;

public interface Message extends Serializable {

	ServentInfo getSenderServentInfo();

	ServentInfo getReceiverServentInfo();

	MessageType getMessageType();

	String getMessageText();
	
	int getMessageId();

	int getChordID();
}
