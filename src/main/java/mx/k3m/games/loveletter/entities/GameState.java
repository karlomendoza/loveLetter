package mx.k3m.games.loveletter.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import mx.k3m.games.loveletter.messages.GameStatusInfoMessage;
import mx.k3m.games.loveletter.websockets.WebSocketGame;

@ApplicationScoped
public class GameState {

	// TODO Quitar sysouts, ponner loggers con level debug y asi

	private List<Player> players;
	private Deck deck;
	private Player playerOnTurn;
	private int playerNumberOnTurn = 0;
	private boolean roundHasEnded;
	private boolean gameInProgress = false;

	public GameState() {

	}

	public void createGame(List<WebSocketGame> playersSession) {
		gameInProgress = true;

		List<Player> players = new LinkedList<Player>();

		playersSession.forEach(playerName -> {
			players.add(new Player(playerName.getUserName()));
		});
		initGameState(players);
	}

	public void initGameState(List<Player> players) {

		this.players = players;
		newRound();
	}

	public void newRound() {
		deck = new Deck();

		for (Player player : players) {
			player.initPlayerForNextRound();
			player.getHand().getCards().add(deck.drawCard(false));
		}

		playerOnTurn = players.get(playerNumberOnTurn);
		takeTurn(playerOnTurn);
		roundHasEnded = false;
	}

	public void takeTurn(Player player) {
		if (player == null || !player.equals(playerOnTurn)) {
			return;
		}
		Card card = deck.drawCard(false);
		player.getHand().getCards().add(card);
		player.removeHandMaidProtection();
		player.setDumpingCard(false);
		return;
	}

	// public boolean takeAction(String playerName, String card, String targetName,
	// String targetCard) {
	// return takeAction(playerName, Card.valueOf(card), targetName,
	// Card.valueOf(targetCard));
	// }

	// public boolean takeAction(String playerName, Card card, String targetName,
	// Card targetCard) {
	public ActionResultMessage takeAction(String playerName, String cardName, String targetName,
			String targetCardName) {
		ActionResultMessage arm = new ActionResultMessage();
		boolean playerCanOnlyDumpCard = false;

		Card card = null;
		Card targetCard = null;

		Player player = null;
		Player target = null;
		for (Player tmpPlayer : players) {
			if (tmpPlayer.getName().equals(playerName))
				player = tmpPlayer;
			if (tmpPlayer.getName().equals(targetName))
				target = tmpPlayer;
		}

		if (player == null) {
			arm.setPrivateMessage("There was an error, please do your action again");
			return arm;
		}

		if (!player.equals(playerOnTurn)) {
			arm.setPrivateMessage("You are not the current player on turn");
			return arm;
		}

		player.removeAllMessages();

		try {
			card = Card.valueOf(cardName);
		} catch (Exception ex) {
			arm.setPrivateMessage("The card you are trying to use does not exists, plz dont eat cheetos and play");
			return arm;
		}

		// check if player has no valid targets and hence is throwing its card
		if ((players.stream().filter(p -> p.getHandMaidProtection()).collect(Collectors.toList())
				.size() == numberOfActivePlayer() - 1)) {
			if (!playerOnTurn.getHand().getCards().contains(Card.PRINCE)
					&& !playerOnTurn.getHand().getCards().contains(Card.HANDMAID)) {
				playerCanOnlyDumpCard = true;
			}
		}

		// onyl make the validations for when he is not throwing the card
		if (!playerCanOnlyDumpCard) {
			boolean cardUsedRequiresTarget = true;
			if (card.equals(Card.HANDMAID) || card.equals(Card.COUNTESS) || card.equals(Card.PRINCESS))
				cardUsedRequiresTarget = false;

			if (card.equals(Card.PRINCE) || card.equals(Card.KING)) {
				if (player.getHand().getCards().contains(Card.COUNTESS)) {
					arm.setPrivateMessage("You can't play the " + card.getName() + " if you have also the "
							+ Card.COUNTESS + " on your hand.");
					return arm;
				}
			}

			if (cardUsedRequiresTarget) {
				if (target == null) {
					arm.setPrivateMessage("You need to select a player as a target");
					return arm;
				}
				if (target.getHandMaidProtection()) {
					arm.setPrivateMessage("Your target has protection from the handMaid,  choose another player");
					return arm;
				}
				if (!target.getActiveInRound()) {
					arm.setPrivateMessage("Your target is out of the round, choose another player");
					return arm;
				}

				// just the prince can be used on himself
				if (!card.equals(Card.PRINCE) && player.equals(target)) {
					arm.setPrivateMessage("The only card that can target yourself is the Prince");
					return arm;
				}
			}

			if (!player.canPlayCard(card)) {
				arm.setPrivateMessage("You don't have the card: " + card.getName() + " in your hand.");
				return arm;
			}

			targetCard = null;
			if (card.equals(Card.GUARD)) {
				try {
					targetCard = Card.valueOf(targetCardName);
				} catch (Exception e) {
					arm.setPrivateMessage(
							"The card: " + targetCardName + " you are trying to guess with the Guard does not exists");
					return arm;
				}
			}
		}

		deck.discard.add(card);
		player.useCardFromHand(card); // use your card

		if (playerCanOnlyDumpCard) {
			player.addMessage("You dumped Card: " + card.getName() + " since there where no valid targets");

			addMessageToPlayers(player, players,
					player.getName() + " dumped card: " + card.getName() + " since he has no valid targets");

		} else {
			switch (card) {
			case GUARD:
				System.out.println("Guardia: " + target.getName() + "card: " + player.getCard() + "your guess: "
						+ targetCard.getName());
				if (target.getCardNumber() == targetCard.getNumber()) {
					deck.discard.add(target.getCard());
					target.setActiveInRound(false);

					player.addMessage("You used a " + card.getName() + " on " + target.getName() + " guessing "
							+ targetCard.getName() + " he is out of the round.");
					addMessageToPlayers(player, players, player.getName() + " used a " + card.getName() + " on "
							+ target.getName() + " guessing " + targetCard.getName() + " he is out of the round.");
				} else {
					player.addMessage("You used a " + card.getName() + " on " + target.getName() + " guessing "
							+ targetCard.getName() + " he did not had that card.");
					addMessageToPlayers(player, players, player.getName() + " used a " + card.getName() + " on "
							+ target.getName() + " guessing " + targetCard.getName() + " he did not had that card.");
				}
				break;
			case PRIEST:
				System.out.println("player: " + target.getName() + "has card: " + target.getCard());
				player.addMessage("You used a " + card.getName() + " on " + target.getName() + " he has card : "
						+ target.getCard());
				target.addMessage(player.getName() + " used a " + card.getName() + " on you, he saw your: "
						+ target.getCard().getName());
				addMessageToPlayers(player, target, players,
						player.getName() + " used a " + card.getName() + " on " + target.getName());
				break;
			case BARON:
				System.out.println("Baron " + target.getName() + " card: " + target.getCard() + " your name: "
						+ player.getName() + " you card: " + player.getCard());

				String playerOut = " Xx is out of the round";
				if (player.getCardNumber() < target.getCardNumber()) {
					deck.discard.add(player.getCard());
					player.setActiveInRound(false);
					playerOut = playerOut.replaceAll("Xx", player.getName());
				} else if (player.getCardNumber() > target.getCardNumber()) {
					deck.discard.add(target.getCard());
					target.setActiveInRound(false);
					playerOut = playerOut.replaceAll("Xx", target.getName());
				} else {
					playerOut = " it's a tie";
				}

				player.addMessage("You used a " + card.getName() + " on " + target.getName() + " he has card: "
						+ target.getCard().getName() + " vs your: " + player.getCard().getName() + playerOut);

				target.addMessage(player.getName() + " used a " + card.getName() + " on you, he has a "
						+ player.getCard().getName() + " vs your: " + target.getCard().getName() + playerOut);

				addMessageToPlayers(player, target, players,
						player.getName() + " used a " + card.getName() + " on " + target.getName() + playerOut);

				break;
			case HANDMAID:
				player.activateHandMaidProtection();
				System.out.println("HandMaid: player has protection");

				player.addMessage("You used a " + card.getName());
				addMessageToPlayers(player, players, player.getName() + " used a " + card.getName());
				break;
			case PRINCE:
				deck.discard.add(target.getCard());
				if (target.getCard().equals(Card.PRINCESS)) {
					target.setActiveInRound(false);

					player.addMessage("You used a " + card.getName() + " on " + target.getName()
							+ " he had the princess and is out.");

					addMessageToPlayers(player, players, player.getName() + " used a " + card.getName() + " on "
							+ target.getName() + " he had the princess and is out.");

				} else {
					System.out.println("principe " + target.getName() + "card: " + target.getCard());

					player.addMessage("You used a " + card.getName() + " on " + target.getName() + " he discarded a "
							+ target.getCard().getName());

					addMessageToPlayers(player, players, player.getName() + " used a " + card.getName() + " on "
							+ target.getName() + " he discarded a " + target.getCard().getName());

					target.discardCard();
					target.getHand().getCards().add(deck.drawCard(true));
				}

				break;
			case KING:
				System.out.println("Rey " + target.getName() + "card: " + target.getCard() + "you card: "
						+ player.getCard() + "your name: " + player.getName());
				Card tmpCard;
				tmpCard = target.getCard();
				target.discardCard();
				target.getHand().getCards().add(player.getCard());
				player.discardCard();
				player.getHand().getCards().add(tmpCard);

				player.addMessage("You used a " + card.getName() + " on " + target.getName() + " you gave a "
						+ target.getCard().getName() + " he gave you a: " + player.getCard().getName());

				target.addMessage(player.getName() + " used a " + card.getName() + " on you, you gave a "
						+ player.getCard().getName() + " he gave you a: " + target.getCard().getName());

				addMessageToPlayers(player, target, players,
						player.getName() + " used a " + card.getName() + " on " + target.getName());
				break;
			case COUNTESS:
				System.out.println("Condesa: ha tirado la condesa, nada pasa");

				player.addMessage("You used a " + card.getName());

				addMessageToPlayers(player, players, player.getName() + " used a: " + card.getName());

				break;
			case PRINCESS:
				System.out.println("Princesa: el jugador pierde el juego");
				player.setActiveInRound(false);

				player.addMessage("You used a " + card.getName() + " you should never throw the princess");
				addMessageToPlayers(player, players, player.getName() + " used a " + card.getName() + " he is out.");
				break;
			default:
				break;
			}
		}

		arm.setValidAction(true);
		if (hasRoundEnded() || deck.cards.size() == 1) {
			Player winnerPlayer = selectWinner();

			addMessageToPlayers(players,
					winnerPlayer.getName() + " won the round with highest card: " + winnerPlayer.getCard().getName());

			if (hasGameEnded()) {
				addMessageToPlayers(players,
						winnerPlayer.getName() + " Won the whole game and a date with the Princess, grats");
				gameInProgress = false;
				return arm;
			}

			addMessageToPlayers(players, "New Round, ready, fight!");
			newRound();
			return arm;
		}

		playerOnTurn = nextActivePlayer();
		takeTurn(playerOnTurn);

		// Check if the player on turn has valid targets for his cards
		// just the prince can be used on himself
		if (players.stream().filter(p -> p.getHandMaidProtection()).collect(Collectors.toList())
				.size() == numberOfActivePlayer() - 1) {
			if (!playerOnTurn.getHand().getCards().contains(Card.PRINCE)
					&& !playerOnTurn.getHand().getCards().contains(Card.HANDMAID)) {
				playerOnTurn.addMessage("No valid targets, you can only throw a card");
				playerOnTurn.setDumpingCard(true);
			}
			if (playerOnTurn.getHand().getCards().contains(Card.PRINCE)) {
				playerOnTurn.addMessage("Since there are no valid targets you can only use the Prince on yourself");
			}
			if (playerOnTurn.getHand().getCards().contains(Card.HANDMAID)) {
				playerOnTurn.addMessage("Since there are no valid targets you can only use the Handmaid on yourself");
			}
		}

		return arm;
	}

	private Player nextActivePlayer() {
		do {
			playerNumberOnTurn = (playerNumberOnTurn + 1) % players.size();
		} while (!players.get(playerNumberOnTurn).getActiveInRound());
		return players.get(playerNumberOnTurn);
	}

	private int numberOfActivePlayer() {
		int playersActive = 0;
		for (Player player : players) {
			if (player.getActiveInRound()) {
				playersActive++;
			}
		}
		return playersActive;
	}

	private boolean hasRoundEnded() {
		if (numberOfActivePlayer() == 1) {
			roundHasEnded = true;
			return true;
		}
		return false;
	}

	private boolean hasGameEnded() {
		int winningNumber = 0;
		switch (players.size()) {
		case 2:
			winningNumber = 2;
			break;
		case 3:
			winningNumber = 5;
			break;
		case 4:
			winningNumber = 4;
			break;
		}
		for (Player player : players) {
			if (player.getNumberOfWins() == winningNumber) {
				return true;
			}
		}
		return false;
	}

	private Player selectWinner() {
		Player playerWithBiggestCard = null;
		for (Player player : players) {
			if (player.getActiveInRound()) {
				if (playerWithBiggestCard == null || playerWithBiggestCard.getCardNumber() < player.getCardNumber()) {
					playerWithBiggestCard = player;
				}
			}
		}
		// TODO checar desempates y empates
		playerWithBiggestCard.scoreAWin();

		return playerWithBiggestCard;
	}

	private Player findPlayer(String playerName) {
		for (Player player : players) {
			if (player.getName().equals(playerName))
				return player;
		}
		return null;
	}

	public GameStatusInfoMessage getPlayerInfo(String playerName) {
		return new GameStatusInfoMessage(findPlayer(playerName), getPlayers(), deck.cards.size(), deck.discard,
				gameInProgress);
	}

	public void addMessageToPlayers(List<Player> players, String message) {
		addMessageToPlayers(null, players, message);
	}

	public void addMessageToPlayers(Player player, List<Player> players, String message) {
		for (Player player2 : players) {
			if (player == null || !player2.equals(player)) {
				player2.addMessage(message);
			}
		}
	}

	public void addMessageToPlayers(Player player, Player player2, List<Player> players, String message) {
		for (Player check : players) {
			if ((player == null || !check.equals(player)) && (player2 == null || !check.equals(player2))) {
				check.addMessage(message);
			}
		}
	}

	public void removeMessagesFromAllPlayers() {
		for (Player player : players) {
			player.removeAllMessages();
		}
	}

	public boolean isRoundHasEnded() {
		return roundHasEnded;
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

	public boolean isGameInProgress() {
		return gameInProgress;
	}

}
