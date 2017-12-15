package mx.k3m.games.loveletter.entities;

public class ActionResultMessage {

	private Boolean validAction;
	private String privateMessage;
	private String publicMessage;

	public ActionResultMessage(Boolean validAction, String privateMessage, String publicMessage) {
		this.validAction = validAction;
		this.privateMessage = privateMessage;
		this.publicMessage = publicMessage;
	}

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

	public String getPublicMessage() {
		return publicMessage;
	}

	public void setPublicMessage(String publicMessage) {
		this.publicMessage = publicMessage;
	}

}
