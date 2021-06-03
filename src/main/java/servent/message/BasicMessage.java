package servent.message;

import app.ChordState;
import app.ServentInfo;
import app.ServentMain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type;
	private final ServentInfo senderServentInfo;
	private final ServentInfo receiverServentInfo;
	private final String messageText;
	private final int chordID;
	
	//This gives us a unique id - incremented in every natural constructor.
	private static final AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;

	public BasicMessage(MessageType type, ServentInfo senderServentInfo, ServentInfo receiverServentInfo) {
		this.type = type;
		this.senderServentInfo = senderServentInfo;
		this.receiverServentInfo = receiverServentInfo;
		this.messageText = "";
		this.chordID = 0;

		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType type, ServentInfo senderServentInfo, ServentInfo receiverServentInfo, int chordID) {
		this.type = type;
		this.senderServentInfo = senderServentInfo;
		this.receiverServentInfo = receiverServentInfo;
		this.messageText = "";
		this.chordID = chordID;
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType type, ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText) {
		this.type = type;
		this.senderServentInfo = senderServentInfo;
		this.receiverServentInfo = receiverServentInfo;
		this.messageText = messageText;
		this.chordID = 0;

		this.messageId = messageCounter.getAndIncrement();
	}

	public BasicMessage(MessageType type, ServentInfo senderServentInfo, ServentInfo receiverServentInfo, String messageText, int chordID) {
		this.type = type;
		this.senderServentInfo = senderServentInfo;
		this.receiverServentInfo = receiverServentInfo;
		this.messageText = messageText;
		this.chordID = chordID;

		this.messageId = messageCounter.getAndIncrement();
	}
	@Override
	public int getChordID() {
		return this.chordID;
	}

	@Override
	public ServentInfo getSenderServentInfo() {
		return this.senderServentInfo;
	}

	@Override
	public ServentInfo getReceiverServentInfo() {
		return this.receiverServentInfo;
	}

	@Override
	public MessageType getMessageType() {
		return type;
	}

	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}
	
	/**
	 * Comparing messages is based on their unique id and the original sender port.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;

			return getMessageId() == other.getMessageId() &&
					getSenderServentInfo().getIpAddress().equals(other.getSenderServentInfo().getIpAddress());
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getSenderServentInfo().getListenerPort());
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|sender_port|message_id|text|type|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[ from: " + ChordState.chordHash(getSenderServentInfo().getIpAddress(), getSenderServentInfo().getListenerPort()) +
				" | " + getSenderServentInfo().getIpAddress() + ":" + getSenderServentInfo().getListenerPort() +
				" | " + getMessageId() +
				" | " + (getMessageText().equals("") ? "no_text" : getMessageText()) +
				" | " + getMessageType() +
				" | to: " + getReceiverServentInfo().getIpAddress() + ":" + getReceiverServentInfo().getListenerPort() +
				" | " + ChordState.chordHash(getReceiverServentInfo().getIpAddress(), getReceiverServentInfo().getListenerPort()) + " ]";
	}

}
