package app;

import servent.message.*;
import servent.message.util.MessageUtil;
import servent.response.PullFileResponse;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(String ip, int port) {
		return ((61 * port) % CHORD_SIZE + ip.hashCode() % CHORD_SIZE) % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private final ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private final List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;


	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg, ServentInfo senderServentInfo) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo(senderServentInfo.getIpAddress(), senderServentInfo.getListenerPort());
		this.valueMap = welcomeMsg.getValues();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_HOST, AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" +
					AppConfig.myServentInfo.getListenerPort() + "\n" +
					AppConfig.myServentInfo.getIpAddress() + "\n"
			);
			
			bsWriter.flush();
			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<ServentInfo> getAllNodeInfo() {
		return this.allNodeInfo;
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public ServentInfo getNextNodeServentInfo() {
		return successorTable[0];
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			return key <= myChordId && key > predecessorChordId;
		} else { //overflow
			return key <= myChordId || key > predecessorChordId;
		}
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);

		updateNodes();
//		allNodeInfo.sort(Comparator.comparingInt(ServentInfo::getChordId));
//
//		List<ServentInfo> newList = new ArrayList<>();
//		List<ServentInfo> newList2 = new ArrayList<>();
//
//		int myId = AppConfig.myServentInfo.getChordId();
//		for (ServentInfo serventInfo : allNodeInfo) {
//			if (serventInfo.getChordId() < myId) {
//				newList2.add(serventInfo);
//			} else {
//				newList.add(serventInfo);
//			}
//		}
//
//		allNodeInfo.clear();
//		allNodeInfo.addAll(newList);
//		allNodeInfo.addAll(newList2);
//		if (newList2.size() > 0) {
//			predecessorInfo = newList2.get(newList2.size()-1);
//		} else {
//			predecessorInfo = newList.get(newList.size()-1);
//		}
//
//		updateSuccessorTable();
	}

	public void removeNodes(List<ServentInfo> oldNodes) {

		AppConfig.timestampedStandardPrint("All nodes before removing: " + allNodeInfo);
//		allNodeInfo.removeAll(oldNodes);
		oldNodes.forEach(nodeToRemove -> {
			allNodeInfo.forEach(node -> {
				if(node.getListenerPort() == nodeToRemove.getListenerPort() &&
							node.getIpAddress().equals(nodeToRemove.getIpAddress())) {
					allNodeInfo.remove(node);
				}
			});
		});
		AppConfig.timestampedStandardPrint("All nodes after removing: " + allNodeInfo);

		updateNodes();
	}

	private void updateNodes() {
		allNodeInfo.sort(Comparator.comparingInt(ServentInfo::getChordId));

		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();

		int myID = AppConfig.myServentInfo.getChordId();
		for(ServentInfo serventInfo : allNodeInfo) {
			if(serventInfo.getChordId() < myID) {
				newList2.add(serventInfo);
				continue;
			}
			newList.add(serventInfo);
		}
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if(newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size() - 1);
		} else {
			predecessorInfo = newList.get(newList.size() - 1);
		}
		updateSuccessorTable();
	}

	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
			return;
		}
		ServentInfo nextNode = getNextNodeForKey(key);
		PutMessage pm = new PutMessage(AppConfig.myServentInfo, nextNode, key, value);
		MessageUtil.sendMessage(pm);
	}
	
	public int getValue(int key) {
		if (isKeyMine(key)) {
			return valueMap.getOrDefault(key, -1);
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo, nextNode, String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}


	private final Map<String, Map<Integer, List<String>>> warehouseFiles = new ConcurrentHashMap<>();
	private final Map<String, List<String>> warehouseDirectoryFiles = new ConcurrentHashMap<>();
	private final Map<String, List<String>> warehouseDirectoryDirectories = new ConcurrentHashMap<>();

	// My implementation
	public void addFile(String filePath, List<String> content) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			AppConfig.timestampedStandardPrint("File content: " + content);

			if(warehouseFiles.containsKey(filePath)) {
				AppConfig.timestampedStandardPrint("Fail");
				// TODO: Send message that file is already added
				return;
			}

			Map<Integer, List<String>> newMap = new ConcurrentHashMap<>();
			newMap.put(0, content);
			warehouseFiles.put(filePath, newMap);

			AppConfig.timestampedStandardPrint("Success");
			// TODO: Send message that file is successfully added

			return;
		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		AddFileMessage addFileMessage = new AddFileMessage(AppConfig.myServentInfo, nextNode, filePath, content);
		MessageUtil.sendMessage(addFileMessage);
	}

	public void addDirectory(String dirPath, List<String> files, List<String> dirs) {
		int dirPathHash = (dirPath.hashCode() > 0 ? dirPath.hashCode() : -dirPath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(dirPathHash) ) {
			if(warehouseDirectoryDirectories.containsKey(dirPath) || warehouseDirectoryFiles.containsKey(dirPath)) {
				AppConfig.timestampedStandardPrint("Dir already in system");
				return;
			}

			warehouseDirectoryFiles.put(dirPath, files);
			warehouseDirectoryDirectories.put(dirPath, dirs);

		}

	}

	public void commitFile(String filePath, List<String> content) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			AppConfig.timestampedStandardPrint("File content: " + content);

			if(!warehouseFiles.containsKey(filePath)) {
				AppConfig.timestampedStandardPrint("Fail");
				//TODO: Send message that file is not being tracked
				return;
			}

			Map<Integer, List<String>> allFileVersions = warehouseFiles.get(filePath);
			int maxVersion = 0;
			for(int key : allFileVersions.keySet()) {
				if(key > maxVersion) {
					maxVersion = key;
				}
			}
			allFileVersions.put(maxVersion, content);
			AppConfig.timestampedStandardPrint("Success");
			// TODO: Send message that commit was successful

			return;
		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		CommitFileMessage commitFileMessage = new CommitFileMessage(AppConfig.myServentInfo, nextNode, filePath, content);
		MessageUtil.sendMessage(commitFileMessage);
}

	public void removeFile(String filePath) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			if(!warehouseFiles.containsKey(filePath)) {
				AppConfig.timestampedStandardPrint("Fail");
				// TODO: Send message that file is not being tracked
				return;
			}

			warehouseFiles.remove(filePath);
			AppConfig.timestampedStandardPrint("Success");
			// TODO: Send message that file is successfully removed


			return;
		}
		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		RemoveFileMessage removeFileMessage = new RemoveFileMessage(AppConfig.myServentInfo, nextNode, filePath);
		MessageUtil.sendMessage(removeFileMessage);
	}

	public PullFileResponse pullFile(String filePath, int version) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			if(!warehouseFiles.containsKey(filePath)) {
				// TODO: Send message that file is not being tracked
				return null;
			}

			Map<Integer, List<String>> allFileVersions = warehouseFiles.get(filePath);

			if(version == -1) {
				for(int key : allFileVersions.keySet()) {
					if(key > version) {
						version = key;
					}
				}
			}
			if(!allFileVersions.containsKey(version)) {
				// TODO: Send message that given version does not exist
				AppConfig.timestampedStandardPrint("Version does not exist");
				return null;
			}

			List<String> fileContent = allFileVersions.get(version);
			return new PullFileResponse(filePath, fileContent);

		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		PullFileAskMessage pullFileAskMessage = new PullFileAskMessage(AppConfig.myServentInfo, nextNode, filePath, version);
		MessageUtil.sendMessage(pullFileAskMessage);

		return new PullFileResponse("", null);
	}

}
