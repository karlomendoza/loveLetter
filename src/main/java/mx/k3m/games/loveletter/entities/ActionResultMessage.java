package mx.k3m.games.loveletter.entities;

public class ActionResultMessage {

	private Boolean validAction;
	private String privateMessage;
	// private List<String> publicMessages;

	public ActionResultMessage() {
		this.validAction = false;
		this.privateMessage = "";
		// this.publicMessages = new ArrayList<>();
	}

	public ActionResultMessage(Boolean validAction, String privateMessage) {
		this.validAction = validAction;
		this.privateMessage = privateMessage;
		// this.publicMessages = publicMessage;
	}

	// public void addPrivateMessage(String message) {
	// this.privateMessages.add(message);
	// }

	// public void addPublicMessage(String message) {
	// this.publicMessages.add(message);
	// }

	public Boolean getValidAction() {
		return validAction;
	}

	public void setValidAction(Boolean validAction) {
		this.validAction = validAction;
	}

	public String getPrivateMessage() {
		return privateMessage;
	}

	public void setPrivateMessage(String privateMessage) {
		this.privateMessage = privateMessage;
	}

	// public List<String> getPublicMessages() {
	// return publicMessages;
	// }
	//
	// public void setPublicMessages(List<String> publicMessages) {
	// this.publicMessages = publicMessages;
	// }

}
