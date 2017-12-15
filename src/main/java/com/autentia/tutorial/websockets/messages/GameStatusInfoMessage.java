package com.autentia.tutorial.websockets.messages;

import java.util.List;
import java.util.stream.Collectors;

import mx.k3m.games.loveletter.entities.Card;
import mx.k3m.games.loveletter.entities.Player;
import mx.k3m.games.loveletter.entities.PlayerPublicInfo;

public class GameStatusInfoMessage {

	// TODO This only needs to have access to the player public info

	private final GameStatus gameStatus;

	public GameStatusInfoMessage(Player user, List<Player> playersInfo, int deckSize, List<Card> discard) {

		List<PlayerPublicInfo> otherPlayers = playersInfo.stream().filter(player -> !player.equals(user))
				.map(player -> new PlayerPublicInfo(player)).collect(Collectors.toList());

		this.gameStatus = new GameStatus(user, otherPlayers, deckSize, discard);
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	class GameStatus {

		private final Player user;
		private int deckSize;
		private final List<PlayerPublicInfo> playersInfo;
		private List<Card> discard;

		private GameStatus(Player user, List<PlayerPublicInfo> playersInfo, int deckSize, List<Card> discard) {
			this.user = user;
			this.playersInfo = playersInfo;
			this.deckSize = deckSize;
			this.discard = discard;
		}

		public Player getUser() {
			return user;
		}

		public List<PlayerPublicInfo> getPlayersInfo() {
			return playersInfo;
		}

		public int getDeckSize() {
			return deckSize;
		}

		public List<Card> getDiscard() {
			return discard;
		}

		public void setDiscard(List<Card> discard) {
			this.discard = discard;
		}
	}
}
