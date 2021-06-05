package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.DistributedMutex;
import app.ServentInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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

			ServentInfo newNodeInfo = clientMessage.getSenderServentInfo();

			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo, clientMessage.getSenderServentInfo());
				MessageUtil.sendMessage(sry);
				return;
			}

			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());


			if (isMyPred) { //if yes, prepare and send welcome message
//				AppConfig.timestampedStandardPrint("New node handler before lock");

				DistributedMutex.lock();
				if(!AppConfig.chordState.isKeyMine(newNodeInfo.getChordId())) {
					DistributedMutex.unlock();
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
					NewNodeMessage nnm = new NewNodeMessage(newNodeInfo, nextNode);
					MessageUtil.sendMessage(nnm);
					return;
				}

//				AppConfig.timestampedStandardPrint("New node handler after lock");


				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}

				AppConfig.chordState.setPredecessor(newNodeInfo);

				Map<String, Map<Integer, List<String>>> myValues = AppConfig.chordState.getWarehouseFiles();
				Map<String, Map<Integer, List<String>>> hisValues = new ConcurrentHashMap<>();

				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();

				for(Entry<String, Map<Integer, List<String>>> valueEntry : myValues.entrySet()) {

					String filePath = valueEntry.getKey();
					int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % ChordState.CHORD_SIZE;

					if(hisPredId == myId) {
						if(myId < newNodeId) {
							if(filePathHash <= newNodeId && filePathHash > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if(filePathHash <= newNodeId || filePathHash > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if(hisPredId < myId) {
						if(filePathHash <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else {
						if(hisPredId > newNodeId) {
							if(filePathHash <= newNodeId || filePathHash > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if(filePathHash <= newNodeId && filePathHash > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
				}



				for (String key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
//				AppConfig.chordState.setValueMap(myValues);

				AppConfig.chordState.setWarehouseFiles(myValues);




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
