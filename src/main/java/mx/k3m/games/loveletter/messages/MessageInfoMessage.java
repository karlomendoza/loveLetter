package mx.k3m.games.loveletter.messages;

public class MessageInfoMessage {

	private final MessageInfo messageInfo;

	public MessageInfoMessage(String from, String message) {
		this.messageInfo = new MessageInfo(from, message);
	}

	public MessageInfo getMessageInfo() {
		return messageInfo;
	}

	public class MessageInfo {

		private final String from;

		private final String message;

		public MessageInfo(String from, String message) {
			this.from = from;
			this.message = message;
		}

		public String getFrom() {
			return from;
		}

		public String getMessage() {
			return message;
		}
	}
}