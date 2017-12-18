package mx.k3m.games.loveletter.websockets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import mx.k3m.games.loveletter.entities.ActionInfoMessage;
import mx.k3m.games.loveletter.entities.ActionResultMessage;
import mx.k3m.games.loveletter.entities.GameState;

@ServerEndpoint(value = "/game")
public class WebSocketGame {

	private static final String GUEST_PREFIX = "Guest";
	private static final AtomicInteger connectionIds = new AtomicInteger(0);
	private static final Set<WebSocketGame> connections = new CopyOnWriteArraySet<>();

	private Session session;

	private String nickname;

	private String userName;

	private Gson jsonProcessor;

	public WebSocketGame() {

	}

	private static final Logger log = LoggerFactory.getLogger(WebSocketGame.class);

	// private static ApplicationContext appCtx = ApplicationContext.instantiate();

	private static GameState gameState = new GameState();

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		connections.add(this);
		// String message = String.format("* %s %s", nickname, "has joined.");
		// broadcast(message);

		this.userName = session.getQueryString().replaceAll("userName=", ""); // TODO checar si se puede sacar el
																				// usuario del usuario
		this.jsonProcessor = new Gson();

		// TODO implementar broadcast messages
		// broadcast(message);
	}

	@OnClose
	public void end(Session session) {
		connections.remove(this);
		String message = String.format("* %s %s", nickname, "has disconnected.");
		broadcast(message);
	}

	@OnMessage
	public void incoming(String message) {
		// TODO make this happen
		// Never trust the client
		// String filteredMessage = HTMLFilter.filter(message.toString());
		// broadcast(filteredMessage);

		// TODO aqui agregar carnita
		if (message.equals("new game")) {
			startNewGame();
			return;
		}

		final ActionInfoMessage actionMessage = jsonProcessor.fromJson(message, ActionInfoMessage.class);
		// If move is not valid
		if (actionMessage.getUser().equals(this.userName)) {
			ActionResultMessage actionResultMessage = gameState.takeAction(actionMessage.getUser(),
					actionMessage.getCardUsed(), actionMessage.getTarget(), actionMessage.getGuardCardGuess());

			if (!actionResultMessage.getValidAction() || (!actionResultMessage.getPrivateMessage().equals(null)
					&& !actionResultMessage.getPrivateMessage().equals(""))) {
				WebSocketGame userConnection = getActionUserConnection(actionMessage.getUser());

				broadcastMessageToUsers(jsonProcessor.toJson(actionResultMessage), userConnection);

				actionResultMessage.setPrivateMessage("");
				if (actionResultMessage.getPublicMessage() != null
						&& !actionResultMessage.getPublicMessage().equals(""))
					broadcastMessageToOtherUsers(jsonProcessor.toJson(actionResultMessage), userConnection);
			}

			if (actionResultMessage.getValidAction())
				sendAllUserNewGameState();
		} else {
			WebSocketGame userConnection = getActionUserConnection(this.userName);
			broadcastMessageToUsers(
					jsonProcessor.toJson(new ActionResultMessage(false, "That's cheating sir", "That's cheating sir")),
					userConnection);
		}

	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		log.error("Chat Error: " + t.toString(), t);
	}

	// protected void onTextMessage(CharBuffer charBuffer) throws IOException {
	// if (charBuffer.toString().equals("new game")) {
	// startNewGame();
	// return;
	// }
	//
	// final ActionInfoMessage actionMessage =
	// jsonProcessor.fromJson(charBuffer.toString(), ActionInfoMessage.class);
	//
	// if (actionMessage.getUser().equals(this.userName)) { // If move is not valid
	// ActionResultMessage actionResultMessage =
	// gameState.takeAction(actionMessage.getUser(),
	// actionMessage.getCardUsed(), actionMessage.getTarget(),
	// actionMessage.getGuardCardGuess());
	//
	// if (!actionResultMessage.getValidAction() ||
	// (!actionResultMessage.getPrivateMessage().equals(null)
	// && !actionResultMessage.getPrivateMessage().equals(""))) {
	// GameConnection userConnection =
	// getActionUserConnection(actionMessage.getUser());
	//
	// userConnection.getWsOutbound()
	// .writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(actionResultMessage)));
	//
	// actionResultMessage.setPrivateMessage("");
	// if (actionResultMessage.getPublicMessage() != null
	// && !actionResultMessage.getPublicMessage().equals(""))
	// sendActionInfoToOtherUsers(actionResultMessage);
	// }
	//
	// if (actionResultMessage.getValidAction())
	// sendAllUserNewGameState();
	// } else {
	// GameConnection userConnection = getActionUserConnection(this.userName);
	// userConnection.getWsOutbound().writeTextMessage(CharBuffer.wrap(jsonProcessor
	// .toJson(new ActionResultMessage(false, "That's cheating sir", "That's
	// cheating sir"))));
	// }
	// }

	private void startNewGame() {
		Set<String> userNames = new HashSet<>();

		connections.forEach(connection -> userNames.add(connection.getUserName()));

		gameState.createGame(userNames);
		sendAllUserNewGameState();
	}

	private void sendAllUserNewGameState() {
		connections.forEach(gameConnection -> {
			try {
				gameConnection.session.getBasicRemote()
						.sendText(jsonProcessor.toJson(gameState.getPlayerInfo(gameConnection.userName)));
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

	/**
	 * private void sendConnectionInfo(WsOutbound outbound) { final List<String>
	 * activeUsers = getActiveUsers(); final ConnectionInfoMessage
	 * connectionInfoMessage = new ConnectionInfoMessage(userName, activeUsers); try
	 * {
	 * outbound.writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(connectionInfoMessage)));
	 * } catch (IOException e) { log.error("No se pudo enviar el mensaje", e); } }
	 * 
	 * private List<String> getActiveUsers() { final List<String> activeUsers = new
	 * ArrayList<String>(); for (GameConnection connection : connections.values()) {
	 * activeUsers.add(connection.getUserName()); } return activeUsers; }
	 * 
	 * // TODO enviar notificacion de conecion a otros usuarios private void
	 * sendActionInfoToOtherUsers(ActionResultMessage message) { final
	 * Collection<GameConnection> otherUsersConnections = connections.values(); for
	 * (GameConnection connection : otherUsersConnections) { try { if
	 * (!connection.equals(this)) {
	 * connection.getWsOutbound().writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(message)));
	 * } } catch (IOException e) { log.error("No se pudo enviar el mensaje", e); } }
	 * }
	 **/

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

	/**
	 * // TODO make this function only return the conections on a room private
	 * GameConnection getAllConnections(String actionStartingUser) { for
	 * (GameConnection connection : connections.values()) { if
	 * (actionStartingUser.equals(connection.getUserName())) { return connection; }
	 * } return null; }
	 **/
	private WebSocketGame getActionUserConnection(String actionStartingUser) {
		for (WebSocketGame connection : connections) {
			if (actionStartingUser.equals(connection.getUserName())) {
				return connection;
			}
		}
		return null;
	}

	private static void broadcast(String jsonMsg) {
		for (WebSocketGame client : connections) {
			try {
				synchronized (client) {
					client.session.getBasicRemote().sendText(jsonMsg);
				}
			} catch (IOException e) {
				log.debug("Chat Error: Failed to send message to client", e);
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
					// Ignore
				}
				String message = String.format("* %s %s", client.nickname, "has been disconnected.");
				broadcast(message);
			}
		}
	}

	private static void broadcastMessageToUsers(String jsonMsg, WebSocketGame user) {
		Set<WebSocketGame> users = new HashSet<>();
		users.add(user);
		broadcastMessageToUsers(jsonMsg, users);
	}

	private static void broadcastMessageToOtherUsers(String jsonMsg, WebSocketGame user) {
		for (WebSocketGame client : connections) {
			if (!client.equals(user)) {
				try {
					synchronized (client) {
						client.session.getBasicRemote().sendText(jsonMsg);
					}
				} catch (IOException e) {
					log.debug("Chat Error: Failed to send message to client", e);
					connections.remove(client);
					try {
						client.session.close();
					} catch (IOException e1) {
						// Ignore
					}
					String message = String.format("* %s %s", client.nickname, "has been disconnected.");
					broadcast(message);
				}
			}
		}
	}

	private static void broadcastMessageToUsers(String jsonMsg, Set<WebSocketGame> usersToSend) {
		for (WebSocketGame client : usersToSend) {
			try {
				synchronized (client) {
					client.session.getBasicRemote().sendText(jsonMsg);
				}
			} catch (IOException e) {
				log.debug("Chat Error: Failed to send message to client", e);
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
					// Ignore
				}
				String message = String.format("* %s %s", client.nickname, "has been disconnected.");
				broadcast(message);
			}
		}
	}

	/**
	 * private void broadcast(String msg) {
	 * 
	 * final List<String> activeUsers = getActiveUsers();
	 * 
	 * for (String client : activeUsers) { try { synchronized (client) { final
	 * ConnectionInfoMessage connectionInfoMessage = new
	 * ConnectionInfoMessage(userName, activeUsers); try {
	 * outbound.writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(connectionInfoMessage)));
	 * } catch (IOException e) { log.error("No se pudo enviar el mensaje", e); }
	 * client.session.getBasicRemote().sendText(msg); } } catch (IOException e) {
	 * log.debug("Chat Error: Failed to send message to client", e);
	 * connections.remove(client); try { client.session.close(); } catch
	 * (IOException e1) { // Ignore } String message = String.format("* %s %s",
	 * client.nickname, "has been disconnected."); broadcast(message); } }
	 **/

}
