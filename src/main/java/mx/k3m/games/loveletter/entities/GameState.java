package mx.k3m.games.loveletter.entities;

import java.util.List;

public class GameState {
	
	//TODO Quitar sysouts, ponner loggers con level debug y asi

	private List<Player> players;
	private Deck deck;
	private Player playerOnTurn;
	private int numberOfPlayers = 4;
	private int playerNumberOnTurn = 0;
	private boolean gameHasEnded;
	
	//TODO there may be a better way to make the player know the card of the other player he saw with the priest
	private Card shownCard;
	private Boolean showCard = false;
	
	public GameState(List<Player> players) {
		deck = new Deck();
		
		this.players = players;
		
		for (Player player : players) {
			player.getHand().getCards().add(deck.drawCard(false));
		}
		
		playerOnTurn = players.get(playerNumberOnTurn);
		takeTurn(playerOnTurn);
		gameHasEnded = false;
	}
	
	
//	public void takeTurn(String playerName) {
//		takeTurn(findPlayer(playerName));
//	}
	
	public Player takeTurn(Player player) {
		if(player == null || !player.equals(playerOnTurn)) {
			return player;
		}
		Card card = deck.drawCard(false);
		if(card == null) {
			//trigger end of game counting;
			endOfGameCounting();
			gameHasEnded = true;
			return player;
		}
		player.getHand().getCards().add(card);
		
		return player;
	}
	
	/*public GameState takeAction(String player, String card, String target, String targetCard) {
		return takeAction(findPlayer(player), Card.valueOf(card) , findPlayer(target), Card.valueOf(targetCard));
	}*/
	
	public GameState takeAction(Player player, Card card, Player target, Card targetCard) {
		if(!player.equals(playerOnTurn)) {
			return null;
		}
		
		player.removeHandMaidProtection();
		
		if(target.getHandMaidProtection())
			return null;
		
		if(!player.canPlayCard(card)) {
			return null;
		}
		
		//just the prince can be used on himself
		if(!card.equals(Card.PRINCE) && player.equals(target))
			return null;
		
		if(target.getHandMaidProtection())
			return null;
		
		player.useCardFromHand(card); //use your card
		setShowCard(false);
		switch (card) {
		case GUARD:
			System.out.println("Guardia: " + target.getName() + "card: " + player.getCard() + "your guess: " + targetCard.getName());
			if(target.getCardNumber() == targetCard.getNumber()) {
				target.setActiveInRound(false);
			}
			break;
		case PRIEST:
			System.out.println("player: " + target.getName() + "has card: " + target.getCard());
			shownCard = target.getCard();
			setShowCard(true);
			break;
		case BARON:
			System.out.println("Baron " + target.getName() + "card: " + target.getCard() + "your name: " + player.getName() + "you card: " + player.getCard() );
			if(player.getCardNumber() < target.getCardNumber()) {
				player.setActiveInRound(false);
			}
			else if (player.getCardNumber() > target.getCardNumber()) {
				target.setActiveInRound(false);
			}
			break;
		case HANDMAID:
			player.activateHandMaidProtection();
			System.out.println("HandMaid: player has protection");
			break;
		case PRINCE:
			target.discardCard();
			target.getHand().getCards().add(deck.drawCard(true));
			System.out.println("principe " + target.getName() + "card: " + target.getCard());
			break;
		case KING:
			System.out.println("Rey " + target.getName() + "card: " + target.getCard() + "you card: " + player.getCard() + "your name: " + player.getName());
			Card tmpCard;
			tmpCard = target.getCard();
			target.discardCard();
			target.getHand().getCards().add(player.getCard());
			player.discardCard();
			player.getHand().getCards().add(tmpCard);
			System.out.println("Rey cambio" + target.getName() + "card: " + targetCard.getName() + "you card: " + player.getCard() + "your name: " + player.getName());
			break;
		case COUNTESS:
			System.out.println("Condesa: ha tirado la condesa, nada pasa");
			break;
		case PRINCESS:
			System.out.println("Princesa: el jugador pierde el juego");
			player.setActiveInRound(false);
			break;
		default:
			break;
		}
		if(hasGameEnded()) {
			endOfGameCounting();
			return this;
		}
		
		playerOnTurn = nextActivePlayer();
		takeTurn(playerOnTurn);
		
		GameStateFacade.getPlayerInfo(this, player);
		return this;
	}
	
	private Player nextActivePlayer() {
		do {
			playerNumberOnTurn = (playerNumberOnTurn +1) % numberOfPlayers; //TODO validate this, should be 0,1,2,3
		} while (!players.get(playerNumberOnTurn).getActiveInRound());
		return players.get(playerNumberOnTurn);
	}
	
	private boolean hasGameEnded() {
		int playersActive = 0;
		for (Player player : players) {
			if(player.getActiveInRound()) {
				playersActive++;
			} 
		}
		if(playersActive == 1) {
			gameHasEnded = true;
			return true;
		}
		return false;
	}
	
	private void endOfGameCounting() {
		Player playerWithBiggestCard = null;
		for (Player player : players) {
			if(player.getActiveInRound())
				if(playerWithBiggestCard == null || playerWithBiggestCard.getCardNumber() < player.getCardNumber())
					playerWithBiggestCard = player;
		}
		//TODO checar desempates y empates
		playerWithBiggestCard.scoreAWin();
	}

	private Player findPlayer(String playerName) {
		for (Player playerSearch : players) {
			if(playerSearch.getName().equals(playerName))
				return playerSearch;
		}
		return null;
	}

	
	public boolean isGameHasEnded() {
		return gameHasEnded;
	}

	public Card getShownCard() {
		return shownCard;
	}

	public void setShownCard(Card shownCard) {
		this.shownCard = shownCard;
	}

	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public Boolean getShowCard() {
		return showCard;
	}

	public void setShowCard(Boolean showCard) {
		this.showCard = showCard;
	}
}
