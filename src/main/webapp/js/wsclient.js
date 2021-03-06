var wsclient = (function() {

    var ws = null;
    var wsURI = 'ws://' + location.host  + '/game';
    
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
        };

        function processMessage(message) {
            if (message.statusInfo) {
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
	            		$('<div class="message"><p>' + $('<p/>').text(userMessages[i]).html() + '</p></div>').hide().prependTo(messages).show('normal');
	        		}
            	}
            	
            	updateGameComponents(message.gameStatus);
            } else if (message.messageInfo){
	            	var userMessage = message.messageInfo.message;
	            	var messages = $("#chatMessages");
            		$('<div class="message"><p>' + $('<p/>').text(userMessage).html() + '</p></div>').hide().prependTo(messages).show('normal');
            	
            } else if(message.roomInfo){
            	if(message.roomInfo.joinedRoom){
            		$("#roomsButtons").remove();
            		$("#createRoom").remove();
            		$('<button id="leaveRoom" onclick="wsclient.leaveRoom(); wsclient.cleanGameArea()">Leave Room </button>').prependTo($("#actionButtons"));
            		$('<button id="newGame" onclick="wsclient.newGame()">New Game </button>').prependTo($("#actionButtons"));
            		
            		var userMessages = message.roomInfo.message;
                	var messages = $("#messages");
              		$('<div class="message"><p>' + $('<p/>').text(userMessages).html() + '</p></div>').hide().prependTo(messages).show('normal');
            	} else {
            		$("#leaveRoom").remove();
            		$("#newGame").remove();
            		$("#createRoom").remove();
            		$('<button id="createRoom" onclick="wsclient.createRoom()">Create New Room</button>').prependTo($("#actionButtons"));
            		
            		if(message.roomInfo.ids != null && message.roomInfo.ids.length > 0){
	            		var ids = message.roomInfo.ids;
	            		
	            		var roomsButtons = $("#roomsButtons");
	        			if(roomsButtons.length == 0){
	        				$('<div id="roomsButtons"></>').appendTo($("#rooms"));
	        			}
	        			roomsButtons = $("#roomsButtons");
	            		if(ids.length == 1){
		            		var id = ids[0];
		            		$('<button id="' + id + '" onclick="wsclient.joinRoom('+id+ ')">Join room ' + id +' </button>').hide().prependTo(roomsButtons).show('normal');
		            		var userMessages = message.roomInfo.message;
		                	var messages = $("#messages");
		              		$('<div class="message"><p>' + $('<p/>').text(userMessages).html() + '</p></div>').hide().prependTo(messages).show('normal');
	            		} else {
	            			for(var i = 0; i < ids.length; i++){
	            				var id = ids[i];
	            				var rooms = $("#rooms");
	            				$('<button id="' + id + '" onclick="wsclient.joinRoom('+id+ ')">Join room ' + id +' </button>').hide().prependTo(roomsButtons).show('normal');
	            			}
	            		}
            		}
            	}
            } else{
            	
            	addOnlineUser()
            	var messages = $("#messages");
            	if(message.privateMessage != ''){
            		$('<div class="message"><p>' + $('<p/>').text(message.privateMessage).html() + '</p></div>').prependTo(messages);
            	}
            }
        }
    }
    
    function roomFunctions(){
    	var rooms = $("#roomsButtons").remove();
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
    
    function cleanGameArea(){
    	$('.removable').remove();
    	$('.selectedPlayer').remove();
    	$('.cardPlayedSelected').remove();
    	if($('.cardGuessSelected')[0]){
    		$('.cardGuessSelected')[0].classList.remove('cardGuessSelected');
    	}
    	$('#cardsSelectorDiv')[0].classList.add('hidden');
    	$("#actionButton").remove();
    	$("#throwCardButton").remove();
    	$("#discardCards").remove();
    	$('#gameAreaContent').remove();
    	$("#deckCards").remove();
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
    		if(gameStatus.user.dumpingCard){
        		attachTriggersToCards = true;
        		$("#actionButton").remove();
            	$("#throwCardButton").remove();
            	var $actionButton = $('<button id="throwCardButton" onclick="throwCard(\'' + gameStatus.user.name + '\');">Throw card! </button>;');
            	$actionButton.appendTo($("#playerArea"));
    		} else if (!gameStatus.user.dumpingCard){
    			attachTriggersToCards = true;
    			$("#throwCardButton").remove();
        		$("#actionButton").remove();
        		var $actionButton = $('<button id="actionButton" onclick="playCard(\'' + gameStatus.user.name + '\');">Play Card </button>;');
        		$actionButton.appendTo($("#playerArea"));
    		}
    	} else {
    		$("#actionButton").remove();
    		$("#throwCardButton").remove();
    		attachTriggersToCards = false;
    	}
    	
    	div.html(extractPlayerInfo(gameStatus.user, attachTriggersToCards, 0, true));
    	
    	div.appendTo($('#playerArea'));
    	
    	for (var i = 0 ; i< players.length ; i++){
    		if(players[i])
    		formatedStatus = formatedStatus + extractPlayerInfo(players[i], false, players.length, false);
    	}
    	
    	$('<div id="gameAreaContent"></>').html(formatedStatus).appendTo($("#gameArea"));
    	
    	
    	//var divPlayers = $(document.createElement('div'));
    	//divPlayers.html(formatedStatus).appendTo($('#gameArea'));
    	
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
    		playerInfo = playerInfo + '<div value="' + player.name + '" onclick="markSelectedUnselected(this, \'selectedPlayer\')" class="player">' + player.name;
    	
    	
    		playerInfo = playerInfo + '<img src="img/player.png" style="width: 50px;"/></div>';
    	} else {
    		playerInfo = playerInfo + '<div value="' + player.name + '" class="player">' + player.name;
    		playerInfo = playerInfo + '<img src="img/playerOutOfRound.png" style="width: 50px;"/></div>';
    	}
    	
    	if(player.hand){
	    	for(var i = 0; i< player.hand.cards.length; i++){
	    		hand = hand + player.hand.cards[i] + '<br/>';
	    		var trigger = ' ';
	    		if(player.dumpingCard){
	    			trigger =  attachSimpleTriggers(player.hand.cards[i]);
	    		} else if(attachTriggersToCards){
	    			trigger = attachTriggers(player.hand.cards[i]);
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
    	return playerInfo;
    }
    
    function attachSimpleTriggers(){
    	return 'onclick="markSelectedUnselected(this, \'cardPlayedSelected\');"'
    }
    
    function attachTriggers(card){
    	if(card == 'GUARD'){
    		return 'onclick="markSelectedUnselected(this, \'cardPlayedSelected\'); markSelectedUnselected($(\'#cardsSelectorDiv\')[0], \'hidden\');"' ;
    	}
    	return 'onclick="markSelectedUnselected(this, \'cardPlayedSelected\');"'
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
    
    function createRoom(){
    	ws.send("create room");
    }
    
    function joinRoom(id){
    	ws.send("join room " + id);
    }
    
    function newGame() {
    	ws.send("new game");
    }
    
    
    function leaveRoom(){
    	ws.send("leaveRoom");
    }
    
    function sendChatMessage(){
    	ws.send("chatMessage" + $("#chatMessage").val());
    	$("#chatMessage").val("");
    }
    
    
    function sendAction(user, target, cardUsed, guardCardGuess){
    	ws.send(JSON.stringify(  {"user" : user, "target" : target, "cardUsed" : cardUsed, "guardCardGuess" : guardCardGuess}  ));
    }

    // metodos publicos
    return {
        connect : connect,
        disconnect : disconnect,
        newGame : newGame,
        sendAction : sendAction,
        createRoom : createRoom,
        joinRoom : joinRoom,
        leaveRoom : leaveRoom,
        cleanGameArea : cleanGameArea,
        sendChatMessage: sendChatMessage,
    };
})();