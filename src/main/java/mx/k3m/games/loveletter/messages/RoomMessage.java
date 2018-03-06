package mx.k3m.games.loveletter.messages;

import java.util.Arrays;
import java.util.List;

public class RoomMessage {

	private final RoomInfo roomInfo;

	public RoomMessage(String message, Boolean joinedRoom) {
		this.roomInfo = new RoomInfo(null, message, joinedRoom);
	}

	public RoomMessage(Integer id, String message, Boolean joinedRoom) {
		this.roomInfo = new RoomInfo(Arrays.asList(id), message, joinedRoom);
	}

	public RoomMessage(List<Integer> ids, String message, Boolean joinedRoom) {
		this.roomInfo = new RoomInfo(ids, message, joinedRoom);
	}

	public RoomInfo getRoomInfo() {
		return roomInfo;
	}

	public class RoomInfo {

		private final List<Integer> ids;
		private final String message;
		private final Boolean joinedRoom;

		public RoomInfo(List<Integer> ids, String message, Boolean joinedRoom) {
			this.ids = ids;
			this.message = message;
			this.joinedRoom = joinedRoom;
		}

		public List<Integer> getIds() {
			return ids;
		}

		public String getMessage() {
			return message;
		}

		public Boolean getJoinedRoom() {
			return joinedRoom;
		}
	}

}
