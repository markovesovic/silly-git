package servent.message;

public enum MessageType {
	PUT,
	ASK_GET,
	TELL_GET,

	PING_MESSAGE,
	PONG_MESSAGE,
	HELPER_PING_MESSAGE,
	REMOVE_NODE_MESSAGE,

	NEW_NODE,
	NEW_NODE_RELEASE_LOCK_MESSAGE,
	WELCOME,
	SORRY,
	UPDATE,
	EXIT_MESSAGE,

	TOKEN_MESSAGE,

	ADD_FILE_MESSAGE,
	COMMIT_FILE_MESSAGE,
	REMOVE_FILE_MESSAGE,
	PULL_FILE_ASK_MESSAGE,
	PULL_FILE_TELL_MESSAGE,
	PUSH_FILE_MESSAGE,
	PUSH_FILE_RESPONSE_MESSAGE,

	ADD_FILE_RESPONSE_MESSAGE,
	REMOVE_FILE_RESPONSE_MESSAGE,
	COMMIT_FILE_RESPONSE_MESSAGE,

	TRANSFER_FILES_MESSAGE
}
