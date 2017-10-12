package mx.k3m.games.loveletter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mx.k3m.games.loveletter.entities.ApplicationContext;
import mx.k3m.games.loveletter.entities.Game;

@RequestMapping
@RestController
public class GameController {

	
	@Autowired
	private Game game;
	
//	@RequestMapping(value = "/avail/{id}", method = RequestMethod.GET)
//	public Integer getProductAvail(@ApiParam(value = "Product ID") @PathVariable("id") int id) {
	
	@RequestMapping(value = "/newGame/{playerName}", method = RequestMethod.POST)
	public String makeNewGame(@PathVariable("playerName") String playerName) {
		game.createGameSinglePlayer(playerName);
		//TODO que regresar aqui
		return "";
	}
	
	@RequestMapping(value = "/takeAction/{playerName}/{cardNumber}/{targetName}/{targetCardNumber}", method = RequestMethod.POST)
	public String takeAction(@PathVariable("playerName") String playerName, @PathVariable("cardNumber") String cardNumber
			,@PathVariable("targetName") String targetName ,@PathVariable("targetCardNumber") String targetCardNumber) {
		game.takeAction(playerName, cardNumber, targetName, targetCardNumber);
//		appCtx.getGameState().takeAction(playerName, cardNumber, targetName, targetCardNumber);

		return "";
	}
}