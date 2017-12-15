package mx.k3m.games.loveletter.entities;

public class PlayerPublicInfo {

	private String name;
	private int numberOfWins;
	private Boolean handMaidProtection;
	private Boolean activeInRound;

	public PlayerPublicInfo(Player player) {
		this.name = player.getName();
		this.numberOfWins = player.getNumberOfWins();
		this.handMaidProtection = player.getHandMaidProtection();
		this.activeInRound = player.getActiveInRound();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfWins() {
		return numberOfWins;
	}

	public void setNumberOfWins(int numberOfWins) {
		this.numberOfWins = numberOfWins;
	}

	public Boolean getActiveInRound() {
		return activeInRound;
	}

	public void setActiveInRound(Boolean activeInRound) {
		this.activeInRound = activeInRound;
	}

	public Boolean getHandMaidProtection() {
		return handMaidProtection;
	}

	public void setHandMaidProtection(Boolean handMaidProtection) {
		this.handMaidProtection = handMaidProtection;
	}

}
