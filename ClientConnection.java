package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.*;

/**
 * 
 * @author Weiguang Ma, Tianning Sun
 *
 */

public class ClientConnection extends Thread {

	// Represents a connection with a client. This class is not only used to
	// determine the code
	// executed by a Thread (the run method), but it is also
	// a 'normal' object, with properties and methods that can be accessed by other
	// objects/threads.
	// Error type 0 1 2 3
	// ACKTYPE 0 1 2 3 4 5
	private Socket clientSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private int clientNum;
	private ArrayList<Room> room;
	private AllPlayer allPlayer = new AllPlayer();
	private JsonObject inputMsg = new JsonObject();// input
	private JsonObject outputMsg = new JsonObject();// output
	private String jsonString;
	private String clientMsg;
	private int MAXPLAYERINAROOM = 6;
	private Player newPlayer, invitedPlayer;
	private JsonParser parser;
	private ServerWindow window;

	public ClientConnection(Socket clientSocket, int clientNum, ArrayList<Room> room,
			AllPlayer allPlayer, ServerWindow window) {
		try {
			this.clientSocket = clientSocket;
			reader = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(
					new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			this.clientNum = clientNum;
			this.room = room;
			this.allPlayer = allPlayer;
			this.window = window;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	// Any code executed within this method will be part of the same execution
	// thread, even
	// if that means invoking methods declared on a class that extends the Thread
	// class.
	public void run() {
		try {
			window.textArea.append(Thread.currentThread().getName()
					+ " - Reading messages from client's " + clientNum + " connection\n");

			clientMsg = null;
			while ((clientMsg = reader.readLine()) != null) {
				window.textArea.append(Thread.currentThread().getName() + " - Message from client "
						+ clientNum + " received: " + clientMsg + "\n");

				parser = new JsonParser();
				inputMsg = (JsonObject) parser.parse(clientMsg);

				if (format(inputMsg.get("Direction").toString()).equals("ClientToServer")) {
					if (format(inputMsg.get("Function").toString()).equals("Register")) { // Player
																							// registration
						if (allPlayer.searchPlayerBasedOnName(
								format(inputMsg.get("Username").toString())) == null) {
							newPlayer = new Player(format(inputMsg.get("Username").toString()),
									clientSocket);
							allPlayer.addNewPlayer(newPlayer);
							window.textField.setText(
									ClientManager.getInstance().getConnectedClients().size() + "");
							ACK(clientMsg, 0); // Ack type 0 register successfully

							WaitListToAllRoom();

							AllRoomToSinglePlayer(
									allPlayer.searchPlayerBasedOnSocket(clientSocket));

						} else {
							Error("Username Exists!", 0);
						}
					} else if (format(inputMsg.get("Function").toString())
							.equals("RequestForRoomDetail")) { // Player enters the game lobby
						if (allPlayer.searchPlayerBasedOnSocket(clientSocket).getRoom() != -1) {
							Error("You are already in another room!", 1);// error type 1
						} else if (room
								.get(Integer
										.parseInt(format(inputMsg.get("RoomNumber").toString())))
								.getRoomStatus() == 1) {
							Error("This room has already began a game!", 2);// error type 2
						} else if (room
								.get(Integer
										.parseInt(format(inputMsg.get("RoomNumber").toString())))
								.getPlayer().size() > MAXPLAYERINAROOM - 1) {
							Error("This room is full!", 3);// error type 3
						} else {
							allPlayer.quitWaitingList(
									allPlayer.searchPlayerBasedOnSocket(clientSocket));
							room.get(
									Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
									.addPlayer(allPlayer.searchPlayerBasedOnSocket(clientSocket));

							ACK(clientMsg, 1); // Ack type 1 enter room successfully

							allPlayer.searchPlayerBasedOnSocket(clientSocket).enterRoom(Integer
									.parseInt(format(inputMsg.get("RoomNumber").toString())));

							SingleRoomToSingleRoom(room.get(Integer
									.parseInt(format(inputMsg.get("RoomNumber").toString()))));

							AllRoomToAllPlayer();

							WaitListToAllRoom();

						}
					} else if (format(inputMsg.get("Function").toString()).equals("Invitation")) { // Room
																									// master
																									// invite
																									// a
																									// player
						if (allPlayer.searchPlayerBasedOnName(
								format(inputMsg.get("Player").toString())) == null) {
							Error("This player doesn't exist!", 4);
						} else if (allPlayer
								.searchPlayerBasedOnName(format(inputMsg.get("Player").toString()))
								.getRoom() != -1) {
							Error("This player has been in another room", 5);
						} else {
							invitedPlayer = allPlayer.searchPlayerBasedOnName(
									format(inputMsg.get("Player").toString()));
							SingleRoomToSinglePlayer(
									room.get(Integer.parseInt(
											format(inputMsg.get("RoomNumber").toString()))),
									invitedPlayer);
							ACK(clientMsg, 2);
						}

					} else if (format(inputMsg.get("Function").toString()).equals("QuitRequest")) {
						if (format(inputMsg.get("Layer").toString()).equals("Room")) { // Quit room
							// to do with game
							Room temp = room.get(Integer
									.parseInt(format(inputMsg.get("RoomNumber").toString())));
							allPlayer.enterWaitingList(temp.searchSocket(clientSocket));
							temp.removePlayer(temp.searchSocket(clientSocket));

							SingleRoomToSingleRoom(temp);

							AllRoomToAllPlayer();

							WaitListToAllRoom();

							ACK("You have quit the room "
									+ format(inputMsg.get("RoomNumber").toString()), 4);
						} else if (format(inputMsg.get("Layer").toString()).equals("Board")) { // Player
																								// quit
																								// during
																								// a
																								// game
							outputMsg = new JsonObject();
							outputMsg.addProperty("Direction", "ServerToClient");
							outputMsg.addProperty("Type", "QuitBoard");
							outputMsg.addProperty("QuitPlayer",
									format(inputMsg.get("QuitPlayer").toString()));
							outputMsg.addProperty("Master",
									room.get(Integer.parseInt(
											format(inputMsg.get("RoomNumber").toString())))
											.getPlayer().get(0).getUsername());

							for (Player p : room
									.get(Integer.parseInt(
											format(inputMsg.get("RoomNumber").toString())))
									.getPlayer()) {
								outputMsg.addProperty("Destination", p.getUsername());
								jsonString = outputMsg.toString();
								p.write(jsonString);
							}
						}

					} else if (format(inputMsg.get("Function").toString()).equals("StartGame")) { // Room
																									// master
																									// starts
																									// a
																									// game
						StartGame(room.get(
								Integer.parseInt(format(inputMsg.get("RoomNumber").toString()))));
						AllRoomToAllPlayer();

						SetLetter(allPlayer.searchPlayerBasedOnSocket(clientSocket));
						for (Player tp : room
								.get(Integer
										.parseInt(format(inputMsg.get("RoomNumber").toString())))
								.getPlayer()) {
							outputMsg = new JsonObject();
							outputMsg.addProperty("Direction", "ServerToClient");
							outputMsg.addProperty("Type", "ACK");
							outputMsg.addProperty("ACKType", "3");
							outputMsg.addProperty("Message", "Your room has started a game");
							jsonString = outputMsg.toString();
							tp.write(jsonString);
						}
					}

					else if (format(inputMsg.get("Function").toString()).equals("InGame")) {
						if (format(inputMsg.get("Operation").toString()).equals("ForVote")) { // All
																								// players
																								// vote
																								// the
																								// words
							room.get(
									Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
									.resetPassCount();
							InGameForVote(inputMsg);
						} else if (format(inputMsg.get("Operation").toString())
								.equals("VoteResult")) {
							InGameUpdateScore(inputMsg);
						} else if (format(inputMsg.get("Operation").toString()).equals("Chat")) {
							InGameChat(inputMsg);
						} else if (format(inputMsg.get("Operation").toString()).equals("Pass")) {
							InGamePass(inputMsg);
						}
					}

					else if ((format(inputMsg.get("Function").toString()).equals("GameOverACK"))) {
						GameRoomReset(inputMsg);
					}

				}
			}
			clientSocket.close();
			ClientManager.getInstance().clientDisconnected(this);
			window.textArea.append(Thread.currentThread().getName() + " - Client " + clientNum
					+ " disconnected\n");
			window.textField.setText(ClientManager.getInstance().getConnectedClients().size() + "");

		} catch (Exception e) {
			Room r;

			try {

				if (allPlayer.searchPlayerBasedOnSocket(clientSocket).getRoom() != -1) { // The
																							// player
																							// is in
																							// a
																							// room
					r = room.get(allPlayer.searchPlayerBasedOnSocket(clientSocket).getRoom());
					if (r.getRoomStatus() == 1) { // The room has began a game
						outputMsg = new JsonObject();
						outputMsg.addProperty("Direction", "ServerToClient");
						outputMsg.addProperty("Type", "QuitBoard");
						outputMsg.addProperty("QuitPlayer",
								allPlayer.searchPlayerBasedOnSocket(clientSocket).getUsername());
						outputMsg.addProperty("Master", r.getPlayer().get(0).getUsername());

						for (Player p : r.getPlayer()) {
							outputMsg.addProperty("Destination", p.getUsername());
							jsonString = outputMsg.toString();
							p.write(jsonString);
						}
						r.removePlayer(allPlayer.searchPlayerBasedOnSocket(clientSocket));
					} else {
						r.removePlayer(allPlayer.searchPlayerBasedOnSocket(clientSocket));

					}

					SingleRoomToSingleRoom(r);
				}
				allPlayer.quitSystem(allPlayer.searchPlayerBasedOnSocket(clientSocket));

				AllRoomToAllPlayer();

				WaitListToAllRoom();

				ClientManager.getInstance().clientDisconnected(this);
				window.textArea.append(Thread.currentThread().getName() + " - Client " + clientNum
						+ " disconnected\n");
				window.textField
						.setText(ClientManager.getInstance().getConnectedClients().size() + "");
			} catch (NullPointerException e1) {
				ClientManager.getInstance().clientDisconnected(this);
				window.textArea.append(Thread.currentThread().getName() + " - Client " + clientNum
						+ " disconnected\n");
			}
		}
	}

	// Game over
	private void GameOver(Room r) {
		r.resetPassCount();
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "GameOver");
		outputMsg.addProperty("RoomNumber", r.getRoomNumber() + "");
		for (Player p : r.getPlayer()) {
			outputMsg.addProperty("Destination", p.getUsername());
			jsonString = outputMsg.toString();
			p.write(jsonString);
		}
	}

	// Game start
	public void StartGame(Room r) {
		r.setRoomStatus(1);
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "StartGame");
		outputMsg.addProperty("RoomNumber", r.getRoomNumber() + "");
		JsonArray PlayerList = new JsonArray();
		JsonObject playerInRoom;
		for (Player p : r.getPlayer()) {
			playerInRoom = new JsonObject();
			playerInRoom.addProperty("PlayerName", p.getUsername());
			PlayerList.add(playerInRoom);
		}
		outputMsg.add("PlayerList", PlayerList);
		for (Player p : r.getPlayer()) {
			outputMsg.addProperty("Destination", p.getUsername());
			jsonString = outputMsg.toString();
			p.write(jsonString);
		}
	}

	public void write(String msg) {
		try {
			writer.write(msg + "\n");
			writer.flush();
			window.textArea.append(Thread.currentThread().getName() + " - Message sent to client "
					+ clientNum + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Invitation message
	public void SingleRoomToSinglePlayer(Room r, Player p) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "SingleRoomToSinglePlayer");
		outputMsg.addProperty("RoomNumber", String.valueOf(room.indexOf(r)));
		outputMsg.addProperty("Message", r.getPlayer().get(0).getUsername()
				+ " invites you to have a game in Room " + room.indexOf(r));
		outputMsg.addProperty("Destination", p.getUsername());
		jsonString = outputMsg.toString();
		p.write(jsonString);

	}

	// Display lobby player information
	public void WaitListToAllRoom() {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "WaitListToAllRoom");
		JsonArray WaitingList = new JsonArray();
		JsonObject waitingPlayer;
		for (Player p : allPlayer.getWaitingPlayer()) {
			waitingPlayer = new JsonObject();
			waitingPlayer.addProperty("PlayerName", p.getUsername());
			WaitingList.add(waitingPlayer);
		}
		outputMsg.add("WaitingList", WaitingList);
		for (Room r : room) {
			for (Player p : r.getPlayer()) {
				outputMsg.addProperty("Destination", p.getUsername());
				jsonString = outputMsg.toString();
				p.write(jsonString);
			}
		}
	}

	// Message acknowledgement
	public void ACK(String msg, int i) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "ACK");
		outputMsg.addProperty("ACKType", String.valueOf(i));
		outputMsg.addProperty("Message", clientMsg);
		jsonString = outputMsg.toString();
		write(jsonString);
	}

	// Player sets a letter
	public void SetLetter(Player p) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "InGame");
		outputMsg.addProperty("Operation", "SetLetter");
		outputMsg.addProperty("RoomNumber", String.valueOf(p.getRoom()));
		outputMsg.addProperty("Destination", p.getUsername());

		jsonString = outputMsg.toString();
		p.write(jsonString);
	}

	// Players vote the words
	public void InGameForVote(JsonObject inputMsg) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "InGame");

		outputMsg.addProperty("Operation", "ForVote");
		outputMsg.addProperty("RoomNumber", format(inputMsg.get("RoomNumber").toString()));
		outputMsg.addProperty("Row", format(inputMsg.get("Row").toString()));
		outputMsg.addProperty("Column", format(inputMsg.get("Column").toString()));
		outputMsg.addProperty("Letter", format(inputMsg.get("Letter").toString()));
		outputMsg.addProperty("ScoredPlayer",
				allPlayer.searchPlayerBasedOnSocket(clientSocket).getUsername());

		for (Player p : room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
				.getPlayer()) {
			outputMsg.addProperty("Destination", p.getUsername());
			jsonString = outputMsg.toString();
			p.write(jsonString);
		}

	}

	// Score update
	public void InGameUpdateScore(JsonObject inputMsg) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "InGame");

		int temp = room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
				.voteResult(inputMsg);
		if (temp != -1) {
			outputMsg.addProperty("Operation", "UpdateScore");
			outputMsg.addProperty("RoomNumber", format(inputMsg.get("RoomNumber").toString()));
			outputMsg.addProperty("Score", temp + "");
			outputMsg.addProperty("ScoredPlayer", format(inputMsg.get("ScoredPlayer").toString()));

			for (Player p : room
					.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
					.getPlayer()) {
				outputMsg.addProperty("Destination", p.getUsername());
				jsonString = outputMsg.toString();
				p.write(jsonString);
			}
			room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString()))).resetVote();
			if (room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
					.getPlayer()
					.indexOf(allPlayer.searchPlayerBasedOnName(
							format(inputMsg.get("ScoredPlayer").toString()))) < room
									.get(Integer.parseInt(
											format(inputMsg.get("RoomNumber").toString())))
									.getPlayer().size() - 1) {
				SetLetter(
						room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
								.getPlayer()
								.get(room
										.get(Integer.parseInt(
												format(inputMsg.get("RoomNumber").toString())))
										.getPlayer()
										.indexOf(allPlayer.searchPlayerBasedOnName(
												format(inputMsg.get("ScoredPlayer").toString())))
										+ 1));
			} else {
				SetLetter(room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
						.getPlayer().get(0));
			}
		}
	}

	// Reset a game room
	public void GameRoomReset(JsonObject inputMsg) {
		Room r = room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())));
		Player p = r.getPlayer().get(0);
		if (r.GameOverACKed()) {
			AllRoomToAllPlayer();
			outputMsg = new JsonObject();
			outputMsg.addProperty("Direction", "ServerToClient");
			outputMsg.addProperty("Type", "GameRoomReset");
			outputMsg.addProperty("CanStart", r.getPlayer().size() > 1 ? "Yes" : "No");
			jsonString = outputMsg.toString();
			p.write(jsonString);

			for (Player tp : r.getPlayer()) {
				outputMsg = new JsonObject();
				outputMsg.addProperty("Direction", "ServerToClient");
				outputMsg.addProperty("Type", "ACK");
				outputMsg.addProperty("ACKType", "5");
				outputMsg.addProperty("Message", "You have quit game in Room " + r.getRoomNumber());
				jsonString = outputMsg.toString();

				tp.write(jsonString);
			}
		}
	};

	// Players choose pass at his/her turn
	public void InGamePass(JsonObject inputMsg) {
		Room r = room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())));
		r.addPassCount();
		if (r.getCount() == r.getPlayer().size()) {
			GameOver(r);
		} else {
			if (r.getPlayer()
					.indexOf(allPlayer.searchPlayerBasedOnName(
							format(inputMsg.get("PassPlayer").toString()))) < r.getPlayer().size()
									- 1) {
				SetLetter(
						r.getPlayer()
								.get(r.getPlayer()
										.indexOf(allPlayer.searchPlayerBasedOnName(
												format(inputMsg.get("PassPlayer").toString())))
										+ 1));
			} else {
				SetLetter(r.getPlayer().get(0));
			}
		}
	}

	// Players chat during a game
	public void InGameChat(JsonObject inputMsg) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "InGame");
		outputMsg.addProperty("Operation", "Chat");

		for (Player p : room.get(Integer.parseInt(format(inputMsg.get("RoomNumber").toString())))
				.getPlayer()) {
			outputMsg.addProperty("Speaker", format(inputMsg.get("Speaker").toString()));
			outputMsg.addProperty("ChatContent", format(inputMsg.get("ChatContent").toString()));
			outputMsg.addProperty("Destination", p.getUsername());
			jsonString = outputMsg.toString();
			p.write(jsonString);
		}
	}

	// Room internal information update
	public void SingleRoomToSingleRoom(Room r) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "SingleRoomToSingleRoom");
		outputMsg.addProperty("RoomNumber", String.valueOf(room.indexOf(r)));
		JsonArray PlayerList = new JsonArray();
		JsonObject playerInRoom;
		for (Player p : r.getPlayer()) {
			playerInRoom = new JsonObject();
			playerInRoom.addProperty("PlayerName", p.getUsername());
			PlayerList.add(playerInRoom);
		}
		outputMsg.add("PlayerList", PlayerList);
		for (Player p : r.getPlayer()) {
			outputMsg.addProperty("Destination", p.getUsername());
			jsonString = outputMsg.toString();
			p.write(jsonString);
		}
	}

	// Displayer all room information to a new player
	public void AllRoomToSinglePlayer(Player p) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "AllRoomToSinglePlayer");
		JsonArray RoomList = new JsonArray();
		JsonObject gameRoom = new JsonObject();
		for (Room r : room) {
			gameRoom = new JsonObject();
			gameRoom.addProperty("RoomNumber", String.valueOf(room.indexOf(r)));
			gameRoom.addProperty("RoomStatus", r.getRoomStatus() == 0 ? "Ready" : "Playing");
			gameRoom.addProperty("PlayerNumber", String.valueOf(r.getPlayer().size()));
			if (r.getPlayer().size() != 0)
				gameRoom.addProperty("RoomMaster", r.getPlayer().get(0).getUsername());
			else
				gameRoom.addProperty("RoomMaster", "None");
			RoomList.add(gameRoom);
		}
		outputMsg.add("RoomList", RoomList);
		outputMsg.addProperty("Destination", p.getUsername());
		jsonString = outputMsg.toString();
		p.write(jsonString);
	}

	// Update room information for all players
	public void AllRoomToAllPlayer() {
		for (Player p : allPlayer.getPlayer()) {
			AllRoomToSinglePlayer(p);
		}
	}

	// Error handle
	public void Error(String msg, int num) {
		outputMsg = new JsonObject();
		outputMsg.addProperty("Direction", "ServerToClient");
		outputMsg.addProperty("Type", "ERROR");
		outputMsg.addProperty("ErrorType", String.valueOf(num));
		outputMsg.addProperty("Detail", msg);
		jsonString = outputMsg.toString();
		write(jsonString);
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}
}
