function markSelectedUnselected(container, styleToSet){
	if(container.classList.contains(styleToSet)){
		container.classList.remove(styleToSet);
	}  else {
		container.classList.add(styleToSet);
	}
}

function checkSelectedPlayer(){
	var selection = $(".selectedPlayer")[0];
	if(!selection){
		return '';
	}
	return selection.getAttribute("value");
}

function getSelectedCard(classToCheckForCardSelection){
	var selection = $("." + classToCheckForCardSelection)[0];
	if(!selection){
		return '';
	}
	return selection.getAttribute('src').replace('img/', '').replace('.png','');
}

function throwCard(playerName){
	var cardNamePlayed = getSelectedCard('cardPlayedSelected');
	if(cardNamePlayed == ''){
		alert('You need to select a card from your hand to play.');
		return false;
	}
	wsclient.sendAction(playerName, '', cardNamePlayed, '');
}

function playCard(playerName){
	var cardNamePlayed = getSelectedCard('cardPlayedSelected');
	if(cardNamePlayed == ''){
		alert('You need to select a card from your hand to play.');
		return false;
	}
	var requiresPlayerSelected = false;
	var requiresCardGuess = false;
	
	switch (cardNamePlayed) {
	case 'GUARD':
		requiresCardGuess = true;
	case 'PRIEST': case 'BARON': case 'PRINCE': case 'KING':
		requiresPlayerSelected = true;
		break;
	case 'HANDMAID': case 'COUNTESS': case 'PRINCESS':
		break;
	default:
		alert('You need to select a valid card to play.');
		return false;
	}
	
	var playerNameSelected = null;
	var cardNameSelected = null;
	
	var playerNameSelected = '';
	if(requiresPlayerSelected){
		playerNameSelected = checkSelectedPlayer();
		if(playerNameSelected == ''){
			if(cardNamePlayed == 'PRINCE') alert('You need to select a player as target. It can be yourself.');
			else alert('You need to select an opponent as target.');
			return false;
		}
	}
	var cardNameGuess = '';
	if(requiresCardGuess){
		cardNameGuess = getSelectedCard('cardGuessSelected');
		if(cardNameGuess == ''){
			alert('You need to select a card that you think your opponent has.');
			return false;
		}
	}
	
	wsclient.sendAction(playerName, playerNameSelected, cardNamePlayed, cardNameGuess);
}

function readyPlayer(){
	if(document.getElementById('readyInput')){
		document.getElementById('readyInput').value="false";
	}  else {
		document.getElementById('readyInput').value="true";
	}
}
