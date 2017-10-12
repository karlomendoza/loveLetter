package mx.k3m.games.loveletter.entities;

public class GameStateFacade{
	
	public static String getPlayerInfo(GameState gm, Player activePlayer) {
		StringBuilder sb = new StringBuilder();
		System.out.println(gm.getDeck().cards.size());
		sb.append(gm.getDeck().cards.size());
		
		for (Player player : gm.getPlayers()) {
			if(player.equals(activePlayer)) {
				System.out.println(player);
				sb.append(player);
			} else {
				System.out.println(player.publicInfo());
				sb.append(player.publicInfo());
			}
			if(gm.getShowCard()) {
				System.out.println(gm.getShownCard());
				sb.append(gm.getShowCard());
			}
		}
		return sb.toString();
	}
}
