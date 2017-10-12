package mx.k3m.games.loveletter.game;

import java.util.LinkedList;
import java.util.List;

import mx.k3m.games.loveletter.entities.Card;
import mx.k3m.games.loveletter.entities.GameState;
import mx.k3m.games.loveletter.entities.Player;

public class NewGameTest {

	public static void main(String args[]) {
		
		List<Player> players = new LinkedList<Player>();
		players.add(new Player("karlo1"));
		players.add(new Player("karlo2"));
		players.add(new Player("karlo3"));
		players.add(new Player("karlo4"));
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
