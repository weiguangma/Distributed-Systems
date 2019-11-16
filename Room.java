package server;

import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.JsonObject;

/**
 * 
 * @author Yu Chao
 *
 */
public class Room {

	private ArrayList<Player> player = new ArrayList<Player>();
	private int roomStatus = 0; // Room Status: ready status(0); playing status(1)
	private int passCount = 0; // Record how many players press the "pass" button in a turn
	private int roomNumber;
	private int VoteACK = 0;
	private int gameOverACK = 0;
	private ArrayList<String> voteWord = new ArrayList<String>();
	private ArrayList<Boolean> voteResult = new ArrayList<Boolean>();

	// Reset voting words
	public void resetVote() {
		voteWord = new ArrayList<String>();
		voteResult = new ArrayList<Boolean>();
	}

	// Confirm all players return to the room
	public boolean GameOverACKed() {
		gameOverACK++;
		if (gameOverACK >= player.size()) {
			setRoomStatus(0);
			gameOverACK = 0;
			return true;
		}
		return false;
	}

	// Vote Result
	public int voteResult(JsonObject msg) {
		int total = msg.getAsJsonArray("VoteWord").size();
		VoteACK++;
		ArrayList<Integer> score = new ArrayList<Integer>();
		if (voteWord.isEmpty()) {
			for (int i = 0; i < total; i++) {
				voteWord.add(format(msg.getAsJsonArray("VoteWord").get(i).getAsJsonObject()
						.get("Word").toString()));
				if (format(msg.getAsJsonArray("VoteWord").get(i).getAsJsonObject().get("IsAWord")
						.toString()).equals("Yes")) {
					voteResult.add(true);
				} else {
					voteResult.add(false);
				}
			}
		} else {
			for (int i = 0; i < total; i++) {
				String tmp = format(msg.getAsJsonArray("VoteWord").get(i).getAsJsonObject()
						.get("Word").toString());
				int temp = voteWord.indexOf(tmp);
				if (voteResult.get(temp) == true && format(msg.getAsJsonArray("VoteWord").get(i)
						.getAsJsonObject().get("IsAWord").toString()).equals("No")) {
					voteResult.set(temp, false);
				}
			}
		}

		if (VoteACK == player.size()) {
			VoteACK = 0;
			for (String s : voteWord) {
				if (voteResult.get(voteWord.indexOf(s)) == true) {
					score.add(s.length());
				}
			}
			if (score.size() == 1)
				return score.get(0);
			else if (score.size() == 0)
				return 0;
			else
				return score.get(0) + score.get(1) == 2 ? 1 : score.get(0) + score.get(1);
		}
		return -1;
	}

	public Room(int i) {
		this.roomNumber = i;
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public void addPlayer(Player newPlayer) {
		this.player.add(newPlayer);
		newPlayer.enterRoom(roomNumber);
	}

	public void removePlayer(Player oldPlayer) {
		this.player.remove(oldPlayer);
		oldPlayer.quitRoom();
	}

	public Player searchSocket(Socket playerSocket) {
		for (int i = 0; i < player.size(); i++) {
			if (playerSocket.equals(player.get(i).getSocket())) {
				return player.get(i);
			}
		}
		return null;
	}

	public Player searchName(String name) {
		for (int i = 0; i < player.size(); i++) {
			if (name.equals(player.get(i).getUsername())) {
				return player.get(i);
			}
		}
		return null;
	}

	public ArrayList<Player> getPlayer() {
		return player;
	}

	public void setRoomStatus(int roomStatus) {
		this.roomStatus = roomStatus;
	}

	public int getRoomStatus() {
		return roomStatus;
	}

	public void addPassCount() {
		passCount += 1;
	}

	public void resetPassCount() {
		passCount = 0;
	}

	public int getCount() {
		return passCount;
	}

	public void write(String string) {
		for (Player p : player)
			p.write(string);
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}

}
