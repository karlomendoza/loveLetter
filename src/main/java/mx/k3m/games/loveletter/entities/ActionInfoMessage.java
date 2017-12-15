package mx.k3m.games.loveletter.entities;

public class ActionInfoMessage {
	
	private String user;
	private String target;
	private String cardUsed;
	private String guardCardGuess;
	
	public ActionInfoMessage(String user, String target, String cardUsed, String guardCardGuess) {
		this.user = user;
		this.target = target;
		this.cardUsed = cardUsed;
		this.guardCardGuess = guardCardGuess;
	}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getCardUsed() {
		return cardUsed;
	}
	public void setCardUsed(String cardUsed) {
		this.cardUsed = cardUsed;
	}
	public String getGuardCardGuess() {
		return guardCardGuess;
	}
	public void setGuardCardGuess(String guardCardGuess) {
		this.guardCardGuess = guardCardGuess;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	

}
