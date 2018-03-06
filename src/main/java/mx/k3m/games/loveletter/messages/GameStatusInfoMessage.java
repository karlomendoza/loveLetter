package mx.k3m.games.loveletter.messages;

import java.util.List;
import java.util.stream.Collectors;

import mx.k3m.games.loveletter.entities.Card;
import mx.k3m.games.loveletter.entities.Player;
import mx.k3m.games.loveletter.entities.PlayerPublicInfo;

public class GameStatusInfoMessage {

	private final GameStatus gameStatus;

	public GameStatusInfoMessage(Player user, List<Player> playersInfo, int deckSize, List<Card> discard,
			Boolean gameInProgress) {

		List<PlayerPublicInfo> otherPlayers = playersInfo.stream().filter(player -> !player.equals(user))
				.map(player -> new PlayerPublicInfo(player)).collect(Collectors.toList());

		this.gameStatus = new GameStatus(user, otherPlayers, deckSize, discard, gameInProgress);
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	class GameStatus {

		private final Player user;
		private int deckSize;
		private final List<PlayerPublicInfo> playersInfo;
		private List<Card> discard;
		private Boolean gameInProgress;

		private GameStatus(Player user, List<PlayerPublicInfo> playersInfo, int deckSize, List<Card> discard,
				Boolean gameInProgress) {
			this.user = user;
			this.playersInfo = playersInfo;
			this.deckSize = deckSize;
			this.discard = discard;
			this.gameInProgress = gameInProgress;
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

		public Boolean getGameInProgress() {
			return gameInProgress;
		}

		public void setGameInProgress(Boolean gameInProgress) {
			this.gameInProgress = gameInProgress;
		}
	}
}
