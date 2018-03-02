var wsclient = (function() {

    var ws = null;
    var wsURI = 'ws://' + location.host  + '/loveletter/game';
    
    var gameStatus = null;
    
    function connect(userName) {

        if(!userName || userName == '') {
            return;
        }

        if ('WebSocket' in window) {
            ws = new WebSocket(wsURI + '?userName=' + userName);
        } else if ('MozWebSocket' in window) {
            ws = new MozWebSocket(wsURI + '?userName=' + userName);
        } else {
            alert('Tu navegador no soporta WebSockets');
            return;
        }
        ws.onopen = function () {
            setConnected(true);
        };
        ws.onmessage = function (event) {
            var message = JSON.parse(event.data);
            processMessage(message);
        };

        ws.onclose = function () {
            setConnected(false);
            document.getElementById('userName').value = '';
            closeAllConversations();
        };

        function processMessage(message) {
            if (message.messageInfo) {
                showConversation(message.messageInfo.from);
                addMessage(message.messageInfo.from, message.messageInfo.message, cleanWhitespaces(message.messageInfo.from) + 'conversation');
            } else if (message.statusInfo) {
                if (message.statusInfo.status == 'CONNECTED') {
                    addOnlineUser(message.statusInfo.user);
                } else if (message.statusInfo.status == 'DISCONNECTED') {
                    removeOnlineUser(message.statusInfo.user);
                }
            } else if (message.connectionInfo) {
                var activeUsers = message.connectionInfo.activeUsers;
                for (var i=0; i<activeUsers.length; i++) {
                    addOnlineUser(activeUsers[i]);
                }
            } else if (message.gameStatus){
            	if(message.gameStatus.user != null){
	            	var userMessages = message.gameStatus.user.messages;
	            	var messages = $("#messages");
	            	for(var i = 0; i < userMessages.length; i++){
	            		$('<div class="message"><p>' + $('<p/>').text(userMessages[i]).html() + '</p></div>').appendTo(messages);
	        		}
	            	var element = document.getElementById('messages');
	            	element.scrollTop = element.scrollHeight - element.clientHeight;
            	}
            	
            	updateGameComponents(message.gameStatus);
            } else if (message.actionStatus){
            	updateGameComponentsOnAction(message.actionStatus);
            } 
            else{
            	var messages = $("#messages");
                
            	if(message.privateMessage != ''){
            		//$actionButton = $('<span >' + message.privateMessage +  '</span><br/>');
            		$('<div class="message"><p>' + $('<p/>').text(message.privateMessage).html() + '</p></div>').appendTo(messages);
            	}
            	else if (message.publicMessage != ''){
            		//$actionButton = $('<span >' + message.publicMessage +  '</span><br/>');
            		$('<div class="message"><p>' + $('<p/>').text(message.publicMessage).html() + '</p></div>').appendTo(messages);
            	}
            	var element = document.getElementById('messages');
            	element.scrollTop = element.scrollHeight - element.clientHeight;
            	//$actionButton.appendTo($("#messageBoardArea"));
            }
        }
    }

    function disconnect() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
        setConnected(false);
    }

    function setConnected(connected) {
        document.getElementById('connect').disabled = connected;
        document.getElementById('disconnect').disabled = !connected;
        cleanConnectedUsers();
        if (connected) {
            updateUserConnected();
        } else {
            updateUserDisconnected();
        }
    }

    function updateUserConnected() {
        var inputUsername = $('#userName');
        var onLineUserName = $('.onLineUserName');
        onLineUserName.html(inputUsername.val());
        inputUsername.css({display:'none'});
        onLineUserName.css({visibility:'visible'});
        $('#status').html('Conectado');
        $('#status').attr({class : 'connected'});
        $('#onLineUsersPanel').css({visibility:'visible'});
    }

    function updateUserDisconnected() {
        $('.onLineUserName').css({visibility:'hidden'});
        $('#userName').css({display:''});
        $('#status').html('Desconectado');
        $('#status').attr({class : 'disconnected'});
        $('#onLineUsersPanel').css({visibility:'hidden'});
    }

    function cleanConnectedUsers() {
        $('#onlineUsers').html('');
    }

    function removeTab(conversationId) {
        $('#conversations').tabs('remove', conversationId);
    }

    function cleanWhitespaces(text) {
        return text.replace(/\s/g,"_");
    }

    function showConversation(from) {
        var conversations = $('#conversations');
        conversations.css({visibility:'visible'});
        var conversationId = cleanWhitespaces(from) + 'conversation';
        if(document.getElementById(conversationId) == null) {
            createConversationPanel(from);
            conversations.tabs('add', '#' + conversationId, from);
        }
        conversations.tabs('select', '#' + conversationId);
        $('#'+conversationId+'message').focus();
    }

    function createConversationPanel(name) {
        var conversationId = cleanWhitespaces(name) + 'conversation';
        var conversationPanel = $(document.createElement('div'));
        conversationPanel.attr({id : conversationId, class : 'conversation'});
        $('<p class="messages"></p><textarea id="' + conversationId + 'message"></textarea>').appendTo(conversationPanel);
        var sendButton = createSendButton(name);
        sendButton.appendTo(conversationPanel);
        var closeButton = createCloseButton(cleanWhitespaces(name));
        closeButton.appendTo(conversationPanel);
        conversationPanel.appendTo($('#conversations'));
    }

    function createSendButton(name) {
        var conversationId = cleanWhitespaces(name) + 'conversation';
        var button = $(document.createElement('button'));
        button.html('Enviar');
        button.click(function () {
            var from = document.getElementById('userName').value;
            var message = document.getElementById(conversationId+'message').value;
            toChat(from, name, message);
            addMessage(from, message, conversationId);
            document.getElementById(conversationId+'message').value = '';
        });
        return button;
    }

    function closeAllConversations() {
        for (var i = $('#conversations').tabs('length'); i >= 0; i--) {
            $('#conversations').tabs('remove', i-1);
        }
        $('#conversations').css({visibility : 'hidden'});
    }

    function createCloseButton(conversationId) {
        var button = $(document.createElement('button'));
        button.html('Cerrar');
        button.click(function () {
            removeTab(conversationId);
        });
        return button;
    }

    function addMessage (from, message, conversationPanelId) {
        var messages = $('#' + conversationPanelId + ' .messages');
        $('<div class="message"><span><b>' + from + '</b> dice:</span><p>' + $('<p/>').text(message).html() + '</p></div>').appendTo(messages);
        messages.scrollTop(messages[0].scrollHeight);
        $('#'+conversationPanelId+' textarea').focus();
    }

    function toChat(sender, receiver, message) {
        ws.send(JSON.stringify({messageInfo : {from : sender, to : receiver, message : message}}));
    }
    
    function updateGameComponentsOnAction(action){
    	if(action.drawCard){
    		for(var i = 0; i <= gameStatus.playersInfo; i++){
    			if(action.player == gameStatus.playersInfo[i]){
    				gameStatus.playersInfo[i].cardsInHand++;
    			}
    		}
    	} else if (action.playCard){
    		//action.result
    		switch (action.playedCard) {
			case "GUARD":
				 
				break;
			case "PRIEST":
				
				break;

			case "BARON":
				
				break;
			case "HANDMAID":
				
				break;
			case "PRINCE":
				
				break;
			case "KING":
				
				break;
			case "COUNTESS":
				
				break;
			case "PRINCESS":
				
				break;
			default:
				break;
			}
    		
		}
    		action.playedCard
    }
    
    function updateGameComponents(gameStatus){
    	this.gameStatus = gameStatus; 
    	$('.removable').remove();
    	$('.selectedPlayer').remove();
    	$('.cardPlayedSelected').remove();
    	
    	
    	if(gameStatus.gameInProgress){
    		$('#newGame').remove();
    	} else {
    		var $actionButton = $('<button id="newGame" onclick="wsclient.newGame();">New Game</button>;');
    		$actionButton.appendTo($("#actionButtons"));
    	}
    	
    	if($('.cardGuessSelected')[0]){
    		$('.cardGuessSelected')[0].classList.remove('cardGuessSelected');
    	}
    	$('#cardsSelectorDiv')[0].classList.add('hidden');
    	
    	var div = $(document.createElement('div'));
    	var players = gameStatus.playersInfo;
    	var formatedStatus ='';
    	var attachTriggersToCards = false;
    	
    	if(gameStatus.user.hand.cards.length == 2){
    		attachTriggersToCards = true;
    		$("#actionButton").remove();
    		var $actionButton = $('<button id="actionButton" onclick="playCard(\'' + gameStatus.user.name + '\');">Do it! </button>;');
    		$actionButton.appendTo($("#actionButtons"));
    	} else {
    		$("#actionButton").remove();
    		attachTriggersToCards = false;
    	}
    	
    	div.html(extractPlayerInfo(gameStatus.user, attachTriggersToCards, 0, true));
    	
    	div.appendTo($('#playerArea'));
    	
    	for (var i = 0 ; i< players.length ; i++){
    		if(players[i])
    		formatedStatus = formatedStatus + extractPlayerInfo(players[i], false, players.length, false);
    	}
    	var divPlayers = $(document.createElement('div'));
    	divPlayers.html(formatedStatus).appendTo($('#gameArea'));
    	
    	if(gameStatus.discard){
    		$("#discardCards").remove();
	    	var divDiscard = $(document.createElement('div'));
	    	divDiscard.attr({id : "discardCards"})
	    	for(var i = 0; i < gameStatus.discard.length; i++ ){
	    		divDiscard.html (divDiscard.html() + '<img style="position:absolute; left:' + (i*15) + 'px; top:' + (i*15) + 'px; z-index: ' + i  + ';" class="discardCards" src="img/' +  gameStatus.discard[i] + '.png"/>');
	    	}
	    	divDiscard.appendTo($('#discard'));
    	}

    	$("#deckCards").remove();
    	var divDeck = $(document.createElement('div'));
    	divDeck.attr({id : "deckCards"})
    	
    	var cardsRemaining = 15 - gameStatus.discard.length - players.length -1 -1;
    	
    	for(var i = 0; i < cardsRemaining; i++ ){
    		divDeck.html(divDeck.html() + '<img style="position:absolute; left:' + (i*3) + 'px; top:' + (i*3) + 'px; z-index: ' + i  + ';" class="deckCards" src="img/cardBack.png"/>');
    	}
    	divDeck.appendTo($('#deck'));
    	
    	
    }
    
    function extractPlayerInfo(player, attachTriggersToCards, numberOfOtherPlayers, isUserRender){
    	var hand = '';
    	var playerInfo = '<div class="removable column"'
    	if(!isUserRender){
    		playerInfo = playerInfo + 'style="width: ' + 94/numberOfOtherPlayers + '%;">'; 
    	} else {
    		playerInfo = playerInfo + 'style="width: 100%">';
    	}
    	
    	if(player.activeInRound){
    		playerInfo = playerInfo + '<div value="' + player.name + '" onclick="markSelectedUnselected(this, \'selectedPlayer\')" class="player">Player Name: ' + player.name;
    	
    	
    		playerInfo = playerInfo + '<img src="img/player.png" style="width: 50px;"/></div>';
    	} else {
    		playerInfo = playerInfo + '<div value="' + player.name + '" class="player">Player Name: ' + player.name;
    		playerInfo = playerInfo + '<img src="img/playerOutOfRound.png" style="width: 50px;"/></div>';
    	}
    	
    	if(player.hand){
	    	for(var i = 0; i< player.hand.cards.length; i++){
	    		hand = hand + player.hand.cards[i] + '<br/>';
	    		var trigger = ' ';
	    		if(attachTriggersToCards){
	    			trigger = attachTriggers(player, player.hand.cards[i]);
	    		}
	    		playerInfo = playerInfo + '<img class="inHandCarts" style="padding-right: 5px;" src="img/' +  player.hand.cards[i] + '.png"' + trigger + '/>';
	    	}
    	} else {
    		for(var i = 0; i< player.cardsInHand; i++){
    			playerInfo = playerInfo + '<img class="inHandCarts" style="padding-right: 5px;" src="img/cardBack.png"/>';
    		}
    	}
    	
    	if(player.handMaidProtection)
    		playerInfo = playerInfo + '<br/>PROTECTED: <img src="img/HANDMAID.png" style="width: 40px;"/>';
    	
    	
    	playerInfo = playerInfo + '<br/>Numero de victorias:' + player.numberOfWins + '</div>';
//    	return '<div class="removable">' +  player.name + '<br/>' + hand + '<br/>' + 'Handmaid Protection:' + player.handMaidProtection +
//    			'<br/>' + 'Jugador aun vivo:' + player.activeInRound + '<br/>' + 
//    			'Numero de victorias:' + player.numberOfWins + '<br/><br/><br/>' + '</div>';
    	return playerInfo;
    }
    
    function attachTriggers(player, card){
    	//'onclick="wsclient.sendAction(' + user + ',' +  target + ',' + card+ ',' + guardCardGuess + ');")'
    	if(card == 'GUARD'){
    		return 'onclick="markSelectedUnselected(this, \'cardPlayedSelected\'); markSelectedUnselected($(\'#cardsSelectorDiv\')[0], \'hidden\');"' ;
    	}
    	return 'onclick="markSelectedUnselected(this, \'cardPlayedSelected\');"'
    	//TODO checar si se ocupa este codigo
//    	switch(card) {
//    	case "GUARD":
//    		return 'onclick="playCard(' + player.name+ ',\'' + card + '\', true,true);"';
//    		break;
//    	case "PRIEST":
//    		return 'onclick="playCard(' + player.name+ ',\'' + card + '\', true,false);"';
//    	case "BARON":
//    		return 'onclick="playCard(' + player.name+ ',\'' + card + '\', true,false);"';
//    	case "PRINCE":
//    		return 'onclick="playCard(' + player.name+ ',\'' + card + '\', true,false);"';
//    	case "KING":
//    		return 'onclick="playCard(' + player.name+ ',\'' + card + '\', true,false);"';
//    		break;
//    	case "HANDMAID":
//    	case "COUNTESS":
//    	case "PRINCESS":
//    		//return 'onclick="wsclient.sendAction();"'
//    		return 'onclick="wsclient.sendAction(\'' + player.name + '\', null' +  '' + ',\'' + card+ '\',' + 'null' + ');"'
//            break;
//        default:
//    	}
    }
    
    /********* usuarios conectados *******/
    function addOnlineUser(userName) {
        var newOnlineUser = createOnlineUser(userName);
        newOnlineUser.appendTo($('#onlineUsers'));
    }

    function removeOnlineUser(userName) {
        $('#onlineUsers > li').each(function (index, elem) {
            if (elem.id == userName + 'onlineuser') {
                $(elem).remove();
            }
        });
    }

    function createOnlineUser(userName) {
        var link = $(document.createElement('a'));
        link.html(userName);
        link.click(function(){
            showConversation(userName);
        });
        var li = $(document.createElement('li'));
        li.attr({id : (userName + 'onlineuser')});
        link.appendTo(li);
        return li;
    }
    
    function newGame() {
    	ws.send("new game");
    }
    
    function ready(ready) {
    	
    	ws.send("Ready: " + ready);
    }
    
//    function sendAction(){
//    	ws.send(document.getElementById('action').value);
//    }
    
    function sendAction(user, target, cardUsed, guardCardGuess){
    	ws.send(JSON.stringify(  {"user" : user, "target" : target, "cardUsed" : cardUsed, "guardCardGuess" : guardCardGuess}  ));
    }

    // metodos publicos
    return {
        connect : connect,
        disconnect : disconnect,
        newGame : newGame,
        sendAction : sendAction,
    };
})();