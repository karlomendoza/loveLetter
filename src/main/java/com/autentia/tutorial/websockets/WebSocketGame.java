package com.autentia.tutorial.websockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autentia.tutorial.websockets.messages.ConnectionInfoMessage;
import com.google.gson.Gson;

import mx.k3m.games.loveletter.entities.ActionInfoMessage;
import mx.k3m.games.loveletter.entities.ActionResultMessage;
import mx.k3m.games.loveletter.entities.GameState;

//@ApplicationScoped
//@ServerEndpoint("/game")
@WebServlet(urlPatterns = "/game")
public class WebSocketGame extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(WebSocketGame.class);

	private static final Map<String, GameConnection> connections = new HashMap<String, GameConnection>();

	// private static ApplicationContext appCtx = ApplicationContext.instantiate();

	private static GameState gameState = new GameState();

	@Override
	protected boolean verifyOrigin(String origin) {
		return true;
	}

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		final String connectionId = request.getSession().getId();
		final String userName = request.getParameter("userName");
		return new GameConnection(connectionId, userName);
	}

	private static class GameConnection extends MessageInbound {

		private final String connectionId;

		private final String userName;

		private final Gson jsonProcessor;

		private GameConnection(String connectionId, String userName) {
			this.connectionId = connectionId;
			this.userName = userName;
			this.jsonProcessor = new Gson();
		}

		@Override
		protected void onOpen(WsOutbound outbound) {
			sendConnectionInfo(outbound);
			// sendStatusInfoToOtherUsers(new StatusInfoMessage(userName,
			// StatusInfoMessage.STATUS.CONNECTED));
			// TODO notificar cuando se conecte un usuaroi
			connections.put(connectionId, this);
		}

		@Override
		protected void onClose(int status) {
			// sendStatusInfoToOtherUsers(new StatusInfoMessage(userName,
			// StatusInfoMessage.STATUS.DISCONNECTED));
			// TODO notificar cuando se vaya un usuario
			connections.remove(connectionId);
		}

		@Override
		protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
			throw new UnsupportedOperationException("Binary messages not supported");
		}

		@Override
		protected void onTextMessage(CharBuffer charBuffer) throws IOException {
			if (charBuffer.toString().equals("new game")) {
				startNewGame();
				return;
			}

			final ActionInfoMessage actionMessage = jsonProcessor.fromJson(charBuffer.toString(),
					ActionInfoMessage.class);
			// TODO WTF lets make this better for starting a new game

			if (actionMessage.getUser().equals(this.userName)) {
				// If move is not valid
				ActionResultMessage actionResultMessage = gameState.takeAction(actionMessage.getUser(),
						actionMessage.getCardUsed(), actionMessage.getTarget(), actionMessage.getGuardCardGuess());

				if (!actionResultMessage.getValidAction() || (!actionResultMessage.getPrivateMessage().equals(null)
						&& !actionResultMessage.getPrivateMessage().equals(""))) {
					GameConnection userConnection = getActionUserConnection(actionMessage.getUser());

					userConnection.getWsOutbound()
							.writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(actionResultMessage)));

					actionResultMessage.setPrivateMessage("");
					if (actionResultMessage.getPublicMessage() != null
							&& !actionResultMessage.getPublicMessage().equals(""))
						sendActionInfoToOtherUsers(actionResultMessage);
				}

				if (actionResultMessage.getValidAction())
					sendAllUserNewGameState();
			} else {
				GameConnection userConnection = getActionUserConnection(this.userName);
				userConnection.getWsOutbound().writeTextMessage(CharBuffer.wrap(jsonProcessor
						.toJson(new ActionResultMessage(false, "That's cheating sir", "That's cheating sir"))));
			}
		}

		private void startNewGame() {
			Set<String> userNames = new HashSet<>();
			connections.values().forEach(connection -> userNames.add(connection.getUserName()));

			gameState.createGame(userNames);
			sendAllUserNewGameState();
		}

		private void sendAllUserNewGameState() {
			connections.forEach((userConnectionId, gameConnection) -> {
				final CharBuffer jsonGameState = CharBuffer
						.wrap(jsonProcessor.toJson(gameState.getPlayerInfo(gameConnection.userName)));
				try {
					gameConnection.getWsOutbound().writeTextMessage(jsonGameState);
				} catch (IOException e) {
					// no idea como fucking fixearias esto, si un usuario se desconecta, rip el
					// game, mandalos todos a la gaver?
					// juat to do, for now, it just does nothing.
					// TODO pensar que diablos hacer aqui
					log.warn("Se está intentando enviar un mensaje a un usuario no conectado");
				}
			});
		}

		public String getUserName() {
			return userName;
		}

		private void sendConnectionInfo(WsOutbound outbound) {
			final List<String> activeUsers = getActiveUsers();
			final ConnectionInfoMessage connectionInfoMessage = new ConnectionInfoMessage(userName, activeUsers);
			try {
				outbound.writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(connectionInfoMessage)));
			} catch (IOException e) {
				log.error("No se pudo enviar el mensaje", e);
			}
		}

		private List<String> getActiveUsers() {
			final List<String> activeUsers = new ArrayList<String>();
			for (GameConnection connection : connections.values()) {
				activeUsers.add(connection.getUserName());
			}
			return activeUsers;
		}

		// TODO enviar notificacion de conecion a otros usuarios
		private void sendActionInfoToOtherUsers(ActionResultMessage message) {
			final Collection<GameConnection> otherUsersConnections = connections.values();
			for (GameConnection connection : otherUsersConnections) {
				try {
					if (!connection.equals(this)) {
						connection.getWsOutbound().writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(message)));
					}
				} catch (IOException e) {
					log.error("No se pudo enviar el mensaje", e);
				}
			}
		}

		// private Collection<GameConnection> getAllChatConnectionsExceptThis() {
		// final Collection<GameConnection> allConnections = connections.values();
		// allConnections.remove(this);
		// return allConnections;
		// }

		// TODO enviar notificacion de conecion a otros usuarios
		// private void sendStatusInfoToOtherUsers(StatusInfoMessage message) {
		// final Collection<GameConnection> otherUsersConnections =
		// getAllChatConnectionsExceptThis();
		// for (GameConnection connection : otherUsersConnections) {
		// try {
		// connection.getWsOutbound().writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(message)));
		// } catch (IOException e) {
		// log.error("No se pudo enviar el mensaje", e);
		// }
		// }
		// }

		// TODO make this function only return the conections on a room
		private GameConnection getAllConnections(String actionStartingUser) {
			for (GameConnection connection : connections.values()) {
				if (actionStartingUser.equals(connection.getUserName())) {
					return connection;
				}
			}
			return null;
		}

		private GameConnection getActionUserConnection(String actionStartingUser) {
			for (GameConnection connection : connections.values()) {
				if (actionStartingUser.equals(connection.getUserName())) {
					return connection;
				}
			}
			return null;
		}
	}

}
