package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NewNodeHandler implements MessageHandler {

	private final Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		try {
			if (clientMessage.getMessageType() != MessageType.NEW_NODE) {
				AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
				return;
			}

			ServentInfo newNodeInfo = new ServentInfo(
					clientMessage.getSenderServentInfo().getIpAddress(),
					clientMessage.getSenderServentInfo().getListenerPort());

			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo, clientMessage.getSenderServentInfo());
				MessageUtil.sendMessage(sry);
				return;
			}

			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());

			if (isMyPred) { //if yes, prepare and send welcome message
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}

				AppConfig.chordState.setPredecessor(newNodeInfo);

				Map<Integer, Integer> myValues = AppConfig.chordState.getValueMap();
				Map<Integer, Integer> hisValues = new HashMap<>();

				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();

				for (Entry<Integer, Integer> valueEntry : myValues.entrySet()) {
					if (hisPredId == myId) { //i am first and he is second
						if (myId < newNodeId) {
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me
						if (valueEntry.getKey() <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { //my old predecesor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { //no new node overflow
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}

					}

				}
				for (Integer key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
				AppConfig.chordState.setValueMap(myValues);

				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo, newNodeInfo, hisValues);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodeInfo, nextNode);
				MessageUtil.sendMessage(nnm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
