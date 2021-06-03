package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeReleaseLockMessage;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class UpdateHandler implements MessageHandler {

	private final Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		try {
			if (clientMessage.getMessageType() != MessageType.UPDATE) {
				AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
				return;
			}

			if (clientMessage.getSenderServentInfo().getListenerPort() != AppConfig.myServentInfo.getListenerPort()
					|| !clientMessage.getSenderServentInfo().getIpAddress().equals(AppConfig.myServentInfo.getIpAddress())) {

				ServentInfo newNodeInfo = clientMessage.getSenderServentInfo();

				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodeInfo);

				AppConfig.chordState.addNodes(newNodes);

				String newMessageText = "";

				if (!clientMessage.getMessageText().equals("")) {

					newMessageText = clientMessage.getMessageText() + ",";

				}

				newMessageText += "{" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "}";


				Message nextUpdate = new UpdateMessage(clientMessage.getSenderServentInfo(),
						AppConfig.chordState.getNextNodeServentInfo(),
						newMessageText);

				MessageUtil.sendMessage(nextUpdate);

				return;
			}
			String messageText = clientMessage.getMessageText();
			String[] ipConfigs = messageText.split(",");

			List<ServentInfo> allNodes = new ArrayList<>();
			for(String ipConfig : ipConfigs) {
				ipConfig = ipConfig.replace("{", "").replace("}", "");
				String ipAddress = ipConfig.split(":")[0];
				String port = ipConfig.split(":")[1];
				allNodes.add(new ServentInfo(ipAddress, Integer.parseInt(port)));
			}
			AppConfig.chordState.addNodes(allNodes);

			NewNodeReleaseLockMessage newNodeReleaseLockMessage = new NewNodeReleaseLockMessage(AppConfig.myServentInfo, AppConfig.chordState.getNextNodeServentInfo());
			MessageUtil.sendMessage(newNodeReleaseLockMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
