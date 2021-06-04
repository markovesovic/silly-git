package servent.message;

import app.ServentInfo;

import java.util.List;
import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<String, Map<Integer, List<String>>> values;
	
	public WelcomeMessage(ServentInfo senderServentInfo, ServentInfo receiverServentInfo, Map<String, Map<Integer, List<String>>> values) {
		super(MessageType.WELCOME, senderServentInfo, receiverServentInfo);
		
		this.values = values;
	}
	
	public Map<String, Map<Integer, List<String>>> getValues() {
		return values;
	}
}
