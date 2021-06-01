package servent.message;

import app.ServentInfo;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<Integer, Integer> values;
	
	public WelcomeMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, Map<Integer, Integer> values) {
		super(MessageType.WELCOME, senderServentInfo, receiverServentInfo);
		
		this.values = values;
	}
	
	public Map<Integer, Integer> getValues() {
		return values;
	}
}
