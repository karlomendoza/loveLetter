package mx.k3m.games.loveletter.entities;

public class Player {

	private String name;
	private Hand hand;
	private int numberOfWins;
	private Boolean handMaidProtection;
	private Boolean activeInRound;

	public Player() {

	}

	public Player(String name) {
		this.name = name;
		this.numberOfWins = 0;
		this.handMaidProtection = false;
		this.hand = new Hand();
		this.activeInRound = true;
	}

	public void initPlayerForNextRound() {
		hand = new Hand();
		handMaidProtection = false;
		activeInRound = true;
	}

	public boolean canPlayCard(Card card) {
		if (!hand.getCards().contains(card)) {
			return false;
		}
		return true;
	}

	public void playCard(Card card) {
		if (canPlayCard(card)) {
			hand.getCards().remove(card);
		}
	}

	public int getCardNumber() {
		if (hand == null || hand.getCards() == null || hand.getCards().get(0) == null)
			return 0;
		return hand.getCards().get(0).getNumber();
	}

	public Card getCard() {
		return hand.getCards().get(0);
	}

	public void scoreAWin() {
		numberOfWins++;
	}

	public void activateHandMaidProtection() {
		handMaidProtection = true;
	}

	public void removeHandMaidProtection() {
		if (handMaidProtection)
			handMaidProtection = false;
	}

	public void discardCard() {
		hand.getCards().clear();
	}

	public void useCardFromHand(Card card) {
		hand.getCards().remove(card);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
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
