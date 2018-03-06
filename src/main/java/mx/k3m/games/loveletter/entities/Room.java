package mx.k3m.games.loveletter.entities;

import java.util.ArrayList;
import java.util.List;

import mx.k3m.games.loveletter.websockets.WebSocketGame;

public class Room {

	private final Integer id;
	private GameState gameState;
	private List<WebSocketGame> players;

	public Room(WebSocketGame player, Integer id) {
		// this.name = name;
		this.setPlayers(new ArrayList<>());
		this.getPlayers().add(player);
		this.gameState = new GameState();
		this.id = id;
	}

	public void startNewGame() {
		this.gameState.createGame(this.getPlayers());
	}

	public boolean isFull() {
		if (getPlayers().size() == 4)
			return true;
		return false;
	}

	public void addPlayer(WebSocketGame player) {
		this.getPlayers().add(player);
	}

	public void removePlayer(WebSocketGame player) {
		this.getPlayers().remove(player);
	}

	public GameState getGameState() {
		return gameState;
	}

	public List<WebSocketGame> getPlayers() {
		return players;
	}

	public void setPlayers(List<WebSocketGame> players) {
		this.players = players;
	}

	public Integer getId() {
		return id;
	}
}
