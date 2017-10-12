package mx.k3m.games.loveletter.entities;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class SinglePlayerGame {

	@Autowired
	ApplicationContext appCtx;
	
	List<Player> players = new LinkedList<Player>();
	
	public void createGameSinglePlayer(String playerName) {
		appCtx.getGameState().newGame(playerName);
	}
	
	public void takeAction(String playerName, String card, String targetName, String targetCard) {
		
		Player player = null;
		Player target = null;
		for (Player tmpPlayer : players) {
			if(tmpPlayer.getName().equals(playerName))
				player = tmpPlayer;
			if(tmpPlayer.getName().equals(targetName))
				target = tmpPlayer;
		}
		if(appCtx.getGameState().takeAction(player, Card.valueOf(card), target, Card.valueOf(targetCard)) != null ) {
			
		}
	}
	
	
	
	public static void main(String args[]) {
			
			List<Player> players = new LinkedList<Player>();
			players.add(new Player("karlo1"));
			players.add(new Player("PC1"));
			players.add(new Player("PC2"));
			players.add(new Player("PC3"));
			GameState gm = new GameState(players);
			
			while(!gm.isGameHasEnded()) {
				for (Player player : players) {
					gm.takeTurn(player);
					
					if(gm.isGameHasEnded())
						break;
					
					for (Player target : players) {
						if(gm.takeAction(player, player.getHand().getCards().get(0), target, Card.PRINCE) != null )
							break;
					}
					
				}
			}
	}

}