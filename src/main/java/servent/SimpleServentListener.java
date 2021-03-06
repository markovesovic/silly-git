package servent;

import app.AppConfig;
import app.Cancellable;
import servent.handler.*;
import servent.message.Message;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;

	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);

				switch (clientMessage.getMessageType()) {
					case PUT:
						messageHandler = new PutHandler(clientMessage);
						break;
					case ASK_GET:
						messageHandler = new AskGetHandler(clientMessage);
						break;
					case TELL_GET:
						messageHandler = new TellGetHandler(clientMessage);
						break;

					case PING_MESSAGE: {
						messageHandler = new PingHandler(clientMessage);
						break;
					}
					case PONG_MESSAGE: {
						messageHandler = new PongHandler(clientMessage);
						break;
					}
					case HELPER_PING_MESSAGE: {
						messageHandler = new HelperPingHandler(clientMessage);
						break;
					}
					case REMOVE_NODE_MESSAGE: {
						messageHandler = new RemoveNodeHandler(clientMessage);
						break;
					}


					case NEW_NODE: {
						messageHandler = new NewNodeHandler(clientMessage);
						break;
					}
					case NEW_NODE_RELEASE_LOCK_MESSAGE: {
						messageHandler = new NewNodeReleaseLockHandler(clientMessage);
						break;
					}
					case WELCOME: {
						messageHandler = new WelcomeHandler(clientMessage);
						break;
					}
					case SORRY: {
						messageHandler = new SorryHandler(clientMessage);
						break;
					}
					case UPDATE: {
						messageHandler = new UpdateHandler(clientMessage);
						break;
					}
					case EXIT_MESSAGE: {
						messageHandler = new ExitHandler(clientMessage, this);
						break;
					}

					case TOKEN_MESSAGE: {
						messageHandler = new TokenHandler(clientMessage);
						break;
					}

					case ADD_FILE_MESSAGE: {
						messageHandler = new AddFileHandler(clientMessage);
						break;
					}
					case COMMIT_FILE_MESSAGE: {
						messageHandler = new CommitFileHandler(clientMessage);
						break;
					}
					case REMOVE_FILE_MESSAGE: {
						messageHandler = new RemoveFileHandler(clientMessage);
						break;
					}
					case PULL_FILE_ASK_MESSAGE: {
						messageHandler = new PullFileAskHandler(clientMessage);
						break;
					}
					case PULL_FILE_TELL_MESSAGE: {
						messageHandler = new PullFileTellHandler(clientMessage);
						break;
					}
					case PUSH_FILE_MESSAGE: {
						messageHandler = new PushFileHandler(clientMessage);
						break;
					}
					case PUSH_FILE_RESPONSE_MESSAGE: {
						messageHandler = new PushFileResponseHandler(clientMessage);
						break;
					}

					case ADD_FILE_RESPONSE_MESSAGE: {
						messageHandler = new AddFileResponseHandler(clientMessage);
						break;
					}
					case REMOVE_FILE_RESPONSE_MESSAGE: {
						messageHandler = new RemoveFileResponseHandler(clientMessage);
						break;
					}
					case COMMIT_FILE_RESPONSE_MESSAGE: {
						messageHandler = new CommitFileResponseHandler(clientMessage);
						break;
					}

					case TRANSFER_FILES_MESSAGE: {
						messageHandler = new TransferFilesHandler(clientMessage);
						break;
					}
				}
				
					threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
