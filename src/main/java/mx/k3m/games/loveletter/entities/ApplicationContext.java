package mx.k3m.games.loveletter.entities;

import org.springframework.context.annotation.Scope;

@Scope("singleton")
public class ApplicationContext {

	private SinglePlayerGame singlePlayerGame;

	public SinglePlayerGame getSinglePlayerGame() {
		if(singlePlayerGame == null)
			return new SinglePlayerGame();
		return singlePlayerGame;
	}

	public void setSinglePlayerGame(SinglePlayerGame singlePlayerGame) {
		this.singlePlayerGame = singlePlayerGame;
	}
	
	
}
