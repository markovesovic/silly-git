package app;

import servent.message.*;
import servent.message.util.MessageUtil;
import servent.response.PullFileResponse;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(String ip, int port) {
		return ((61 * port) % CHORD_SIZE + ip.hashCode() % CHORD_SIZE) % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;


	public ChordState() {
		initChordState();
	}

	public void initChordState() {
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
//		this.valueMap = welcomeMsg.getValues();
		this.warehouseFiles = welcomeMsg.getValues();
		AppConfig.timestampedStandardPrint("My new files");
		AppConfig.timestampedStandardPrint(warehouseFiles.toString());

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
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
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

	public void setWarehouseFiles(Map<String, Map<Integer, List<String>>> warehouseFiles) {
		this.warehouseFiles = warehouseFiles;
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
	}

	public void removeNodes(List<ServentInfo> oldNodes) {

		AppConfig.timestampedStandardPrint("All nodes before removing: " + allNodeInfo);
		oldNodes.forEach(nodeToRemove ->
				allNodeInfo.forEach(node -> {
					if(node.getListenerPort() == nodeToRemove.getListenerPort() && node.getIpAddress().equals(nodeToRemove.getIpAddress()))
							allNodeInfo.remove(node);
		}));
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
		} else if (newList.size() > 0) {
			predecessorInfo = newList.get(newList.size() - 1);
		} else {
			initChordState();
			return;
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

	private final Map<String, Integer> currentFileVersionsInWorkingDir = new ConcurrentHashMap<>();

	private Map<String, Map<Integer, List<String>>> warehouseFiles = new ConcurrentHashMap<>();
//	private final Map<String, List<String>> warehouseDirectoryFiles = new ConcurrentHashMap<>();
//	private final Map<String, List<String>> warehouseDirectoryDirectories = new ConcurrentHashMap<>();

	// My implementation
	public void addFile(String filePath, List<String> content, int chordID) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			AppConfig.timestampedStandardPrint("File content: " + content);

			ServentInfo nextNode = getNextNodeForKey(chordID);
			String notification;

			if(warehouseFiles.containsKey(filePath)) {
				notification = "File is already being tracked in system! Try commit";
			} else {
				notification = "File has been successfully added to system!";

				Map<Integer, List<String>> newMap = new ConcurrentHashMap<>();
				newMap.put(0, content);
				warehouseFiles.put(filePath, newMap);
			}

			AddFileResponseMessage addFileResponseMessage = new AddFileResponseMessage(AppConfig.myServentInfo, nextNode, notification, chordID);
			MessageUtil.sendMessage(addFileResponseMessage);

			return;
		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		AddFileMessage addFileMessage = new AddFileMessage(AppConfig.myServentInfo, nextNode, filePath, content, chordID);
		MessageUtil.sendMessage(addFileMessage);
	}

	/*
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
	*/

	public void commitFile(String filePath, List<String> content, int version, int chordID) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			AppConfig.timestampedStandardPrint("File content: " + content);

			ServentInfo nextNode = getNextNodeForKey(chordID);
			String notification;
			boolean conflict = false;
			List<String> returnContent = null;

			if(!warehouseFiles.containsKey(filePath)) {
				notification = "Given file is not yet being tracked! Use add first";
			} else {

				Map<Integer, List<String>> allFileVersions = warehouseFiles.get(filePath);

				int maxVersion = 0;
				for (int key : allFileVersions.keySet()) {
					if (key > maxVersion) {
						maxVersion = key;
					}
				}

				if (maxVersion > version) {
					notification = "Your local copy is behind remote by " + (maxVersion - version) + " commit(s)";
					conflict = true;
					returnContent = allFileVersions.get(maxVersion);

				} else if (content.equals(allFileVersions.get(maxVersion))) {
					notification = "Commit failed because file did not change since latest commit";
				} else {
					notification = "File successfully committed, latest version: " + (maxVersion + 1);
					allFileVersions.put(maxVersion + 1, content);
				}
			}

			CommitFileResponseMessage commitFileResponseMessage = new CommitFileResponseMessage(
					AppConfig.myServentInfo, nextNode, notification, chordID, conflict, returnContent, filePath
			);
			MessageUtil.sendMessage(commitFileResponseMessage);

			return;
		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		CommitFileMessage commitFileMessage = new CommitFileMessage(AppConfig.myServentInfo, nextNode, filePath, content, version, chordID);
		MessageUtil.sendMessage(commitFileMessage);
}

	public void removeFile(String filePath, int chordID) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			ServentInfo nextNode = getNextNodeForKey(chordID);
			String notification;
			if(!warehouseFiles.containsKey(filePath)) {
				notification = "This file is not being tracked in system!";
			} else {
				warehouseFiles.remove(filePath);
				notification = "File has been successfully removed from system";
			}
			RemoveFileResponseMessage removeFileResponseMessage = new RemoveFileResponseMessage(AppConfig.myServentInfo, nextNode, notification, chordID);
			MessageUtil.sendMessage(removeFileResponseMessage);

			return;
		}
		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		RemoveFileMessage removeFileMessage = new RemoveFileMessage(AppConfig.myServentInfo, nextNode, filePath, chordID);
		MessageUtil.sendMessage(removeFileMessage);
	}


	public PullFileResponse pullFile(String filePath, int version, int chordID) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			if(!warehouseFiles.containsKey(filePath)) {
				return new PullFileResponse("File is not being tracked in system!", null, -1);
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
				return new PullFileResponse("Asked file version does not exist!", null, -1);
			}

			List<String> fileContent = allFileVersions.get(version);
			return new PullFileResponse(filePath, fileContent, version);
		}

		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		PullFileAskMessage pullFileAskMessage = new PullFileAskMessage(AppConfig.myServentInfo, nextNode, filePath, version, chordID);
		MessageUtil.sendMessage(pullFileAskMessage);

		return new PullFileResponse("", null, -2);
	}

	public void pushFile(String filePath, List<String> content, int chordID) {
		int filePathHash = (filePath.hashCode() > 0 ? filePath.hashCode() : -filePath.hashCode()) % CHORD_SIZE;

		if( isKeyMine(filePathHash) ) {

			AppConfig.timestampedStandardPrint("File content: " + content);

			ServentInfo nextNode = getNextNodeForKey(chordID);
			String notification;

			int maxVersion = -1;

			if(!warehouseFiles.containsKey(filePath)) {
				notification = "Given file is not being tracked!!";
			} else {
				Map<Integer, List<String>> allFileVersions = warehouseFiles.get(filePath);

				for(int key : allFileVersions.keySet()) {
					if(key > maxVersion) {
						maxVersion = key;
					}
				}
				allFileVersions.put(maxVersion + 1, content);
				notification = "Successfully updated remote copy";
			}
			PushFileResponseMessage pushFileResponseMessage = new PushFileResponseMessage(
					AppConfig.myServentInfo, nextNode, notification, chordID, filePath, maxVersion + 1
			);
			MessageUtil.sendMessage(pushFileResponseMessage);

			return;
		}
		ServentInfo nextNode = getNextNodeForKey(filePathHash);
		PushFileMessage pushFileMessage = new PushFileMessage(AppConfig.myServentInfo, nextNode, filePath, content, chordID);
		MessageUtil.sendMessage(pushFileMessage);
	}

	public void saveFileToFs(String filePath, List<String> content) {
		try {
			File file = new File(AppConfig.ROOT_PATH + filePath);
			file.getParentFile().mkdirs();

			FileWriter fileWriter = new FileWriter(AppConfig.ROOT_PATH + filePath);

			for(String row : content) {
				fileWriter.write(row + System.lineSeparator());
			}

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Integer> getCurrentFileVersionsInWorkingDir() {
		return currentFileVersionsInWorkingDir;
	}

	public Map<String, Map<Integer, List<String>>> getWarehouseFiles() {
		return warehouseFiles;
	}

}
