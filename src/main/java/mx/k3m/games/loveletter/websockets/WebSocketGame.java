package mx.k3m.games.loveletter.websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import mx.k3m.games.loveletter.entities.Room;
import mx.k3m.games.loveletter.messages.RoomMessage;

@ServerEndpoint(value = "/game")
public class WebSocketGame {

	private static final AtomicInteger roomIds = new AtomicInteger(0);
	private static final Set<WebSocketGame> connections = new CopyOnWriteArraySet<>();
	private static Map<Integer, Room> rooms = new HashMap<>();

	private Session session;

	private String nickname;

	private String userName;

	private Room room;

	private Gson jsonProcessor;

	public WebSocketGame() {

	}

	private static final Logger log = LoggerFactory.getLogger(WebSocketGame.class);

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		connections.add(this);
		// String message = String.format("* %s %s", nickname, "has joined.");
		// broadcast(message);

		this.room = null;
		this.userName = session.getQueryString().replaceAll("userName=", "");
		this.jsonProcessor = new Gson();

		sendRoomIdsToUser(this);

		// TODO implementar broadcast messages
		// broadcast(message);
	}

	@OnClose
	public void end(Session session) {
		connections.remove(this);
		String message = String.format("* %s %s", nickname, "has disconnected.");
		broadcast(message);

		canCloseRoom();
	}

	@OnMessage
	public void incoming(String message) {
		// TODO make this happen
		// Never trust the client
		// String filteredMessage = HTMLFilter.filter(message.toString());
		// broadcast(filteredMessage);
		WebSocketGame userConnection = getActionUserConnection(this.userName);

		if (message.equals("getRooms")) {
			sendRoomIdsToUser(userConnection);
		}

		if (message.equals("create room")) {
			if (this.room != null) {
				broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "You can only create a room when you are not on one")),
						userConnection);
				return;
			}

			int id = roomIds.getAndIncrement();
			Room room = new Room(this, id);
			userConnection.room = room;

			rooms.put(id, room);
			broadcastMessageToUser(jsonProcessor.toJson(new RoomMessage("You joined room " + id, true)), userConnection);

			notifyRoomCreation(jsonProcessor.toJson(new RoomMessage(id, this.userName + " created new room: " + id, false)), connections);
			return;
		}

		if (message.startsWith("join room")) {
			String roomNumber = message.replace("join room ", "");

			try {
				Room room = rooms.get(Integer.valueOf(roomNumber));
				if (room.isFull()) {
					broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "Room is full, join another one or create your own")),
							userConnection);
				}

				broadcastMessageToUsers(jsonProcessor.toJson(new ActionResultMessage(false, this.userName + " joined the room")),
						new HashSet<>(room.getPlayers()));

				userConnection.room = room;
				room.addPlayer(this);
				broadcastMessageToUser(jsonProcessor.toJson(new RoomMessage("You joined room " + roomNumber, true)), userConnection);
			} catch (Exception ex) {
				broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "There was an error, please try again")), userConnection);
			}
			return;
		}

		// After this all actions should happen in a room, not being in a room is an error
		if (this.room == null) {
			broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "You are not in a room, get in a room to do this")), userConnection);
			return;
		}

		if (message.equals("leaveRoom")) {
			broadcastMessageToUsers(jsonProcessor.toJson(new ActionResultMessage(false, this.userName + " left the room")), new HashSet<>(room.getPlayers()));
			canCloseRoom();
			this.room = null;
			sendRoomIdsToUser(userConnection);
			return;
		}

		if (message.equals("new game")) {
			if (userConnection.room.getGameState().isGameInProgress()) {
				broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "Can't start a game, one already in progress")), userConnection);
				return;
			}
			if (userConnection.room.getPlayers().size() == 1) {
				broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "Can't start a game, you are the only player in the room")),
						userConnection);
				return;
			}

			this.room.startNewGame();
			sendAllUserNewGameState(this.room);

			return;
		}

		final ActionInfoMessage actionMessage = jsonProcessor.fromJson(message, ActionInfoMessage.class);
		// If move is not valid
		if (actionMessage.getUser().equals(this.userName)) {
			ActionResultMessage actionResultMessage = userConnection.room.getGameState().takeAction(actionMessage.getUser(), actionMessage.getCardUsed(),
					actionMessage.getTarget(), actionMessage.getGuardCardGuess());

			if (!actionResultMessage.getValidAction()) {
				broadcastMessageToUser(jsonProcessor.toJson(actionResultMessage), userConnection);
			}

			if (actionResultMessage.getValidAction()) {
				sendAllUserNewGameState(this.room);
				userConnection.room.getGameState().removeMessagesFromAllPlayers();
			}
		} else {
			broadcastMessageToUser(jsonProcessor.toJson(new ActionResultMessage(false, "That's cheating sir")), userConnection);
		}

	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		log.error("Chat Error: " + t.toString(), t);
	}

	private void sendAllUserNewGameState(Room room) {
		room.getPlayers().forEach(gameConnection -> {
			try {
				gameConnection.session.getBasicRemote().sendText(jsonProcessor.toJson(room.getGameState().getPlayerInfo(gameConnection.userName)));
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

	private void sendRoomIdsToUser(WebSocketGame userConnection) {
		List<Integer> roomsForButtons = new ArrayList<>();
		for (Integer id : rooms.keySet()) {
			roomsForButtons.add(id);
		}
		broadcastMessageToUser(jsonProcessor.toJson(new RoomMessage(roomsForButtons, null, false)), userConnection);
	}

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

	private static void broadcastMessageToUser(String jsonMsg, WebSocketGame user) {
		Set<WebSocketGame> users = new HashSet<>();
		users.add(user);
		broadcastMessageToUsers(jsonMsg, users);
	}

	private static void notifyRoomCreation(String jsonMsg, Set<WebSocketGame> usersToSend) {
		for (WebSocketGame client : usersToSend) {
			if (client.room == null) {
				try {
					client.session.getBasicRemote().sendText(jsonMsg);
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

	private void canCloseRoom() {
		if (room != null) {
			room.getPlayers().remove(this);
			if (room.getPlayers().isEmpty())
				rooms.remove(this.room.getId());
		}
	}

	/**
	 * private void broadcast(String msg) {
	 * 
	 * final List<String> activeUsers = getActiveUsers();
	 * 
	 * for (String client : activeUsers) { try { synchronized (client) { final ConnectionInfoMessage connectionInfoMessage = new
	 * ConnectionInfoMessage(userName, activeUsers); try { outbound.writeTextMessage(CharBuffer.wrap(jsonProcessor.toJson(connectionInfoMessage))); }
	 * catch (IOException e) { log.error("No se pudo enviar el mensaje", e); } client.session.getBasicRemote().sendText(msg); } } catch (IOException e) {
	 * log.debug("Chat Error: Failed to send message to client", e); connections.remove(client); try { client.session.close(); } catch (IOException e1) {
	 * // Ignore } String message = String.format("* %s %s", client.nickname, "has been disconnected."); broadcast(message); } }
	 **/

}
