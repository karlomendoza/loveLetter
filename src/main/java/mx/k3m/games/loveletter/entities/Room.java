package mx.k3m.games.loveletter.entities;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private GameState gameState;
	private List<Player> players;
	private String name;

	public Room(Player player, String name) {
		this.name = name;
		this.players = new ArrayList<>();
		this.players.add(player);
		this.gameState = new GameState();
	}

	public void addPlayer(Player player) {
		this.players.add(player);
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
}
