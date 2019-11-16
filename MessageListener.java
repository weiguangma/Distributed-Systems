package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.SocketException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author Weiguang Ma, Tianning Sun
 *
 */
public class MessageListener extends Thread {

	private BufferedReader reader;
	private BufferedWriter writer;
	private JsonObject inputMsg = new JsonObject();// input

	public MessageListener(BufferedWriter writer, BufferedReader reader) {
		this.writer = writer;
		this.reader = reader;
	}

	@Override
	public void run() {

		try {
			String msg = null;
			ClientRegister clientRegister = new ClientRegister(writer);
			ClientRoomList clientRoomList = new ClientRoomList(writer);
			ClientGameRoom clientGameRoom = new ClientGameRoom(writer);
			ErrorPage errorPage = new ErrorPage();
			GameBoard gameBoard = new GameBoard(writer);
			clientRegister.initialize();

			// Read messages from the server while the end of the stream is not reached
			while ((msg = reader.readLine()) != null) {
				System.out.println(msg);

				JsonParser parser = new JsonParser();
				inputMsg = (JsonObject) parser.parse(msg);
				if (format(inputMsg.get("Direction").toString()).equals("ServerToClient")) {

					if (format(inputMsg.get("Type").toString()).equals("ERROR")) {
						errorPage = new ErrorPage();
						errorPage.display(inputMsg);
						// Input Error handling
						if (format(inputMsg.get("ErrorType").toString()).equals("0")) {
							clientRegister.resetUsername();
						} else if (format(inputMsg.get("ErrorType").toString()).equals("1")
								|| format(inputMsg.get("ErrorType").toString()).equals("2")
								|| format(inputMsg.get("ErrorType").toString()).equals("3"))
							clientRoomList.resetRoomNumber();
						else if (format(inputMsg.get("ErrorType").toString()).equals("4")
								|| format(inputMsg.get("ErrorType").toString()).equals("5"))
							clientGameRoom.resetInvitedPlayer();
					}

					else if (format(inputMsg.get("Type").toString()).equals("ACK")) {
						if (format(inputMsg.get("ACKType").toString()).equals("0")) {
							clientRegister.close(); // Show all rooms information and close the
													// register interface
							clientRoomList.initialize(); // Open the game lobby interface
						} else if (format(inputMsg.get("ACKType").toString()).equals("1")) {
							clientRoomList.setInvisiable();
							clientGameRoom.initialize(); // Open the game room interface
						} else if (format(inputMsg.get("ACKType").toString()).equals("2")) {
							clientGameRoom.invitationACK(); // Display the invitation confirm
															// message
						} else if (format(inputMsg.get("ACKType").toString()).equals("3")) {
							clientGameRoom.setInvisiable(); // Close the game room interface
						} else if (format(inputMsg.get("ACKType").toString()).equals("4")) {
							clientRoomList.setVisiable(); // Open the game lobby interface
						} else if (format(inputMsg.get("ACKType").toString()).equals("5")) {
							clientGameRoom.setVisiable(); // Open the game room interface
						}

					}

					else if (format(inputMsg.get("Type").toString())
							.equals("AllRoomToSinglePlayer")) {
						clientRoomList.displayInfo(inputMsg); // Display all room information
					} else if (format(inputMsg.get("Type").toString())
							.equals("SingleRoomToSingleRoom")) {
						clientGameRoom.display(inputMsg); // Display game room information
					} else if (format(inputMsg.get("Type").toString())
							.equals("WaitListToAllRoom")) {
						clientGameRoom.displayWaitingList(inputMsg); // Players in the game lobby
																		// are displayed in the game
																		// room
					} else if (format(inputMsg.get("Type").toString())
							.equals("SingleRoomToSinglePlayer")) {
						clientRoomList.displayInvitation(inputMsg); // Display invitation
																	// information
					} else if (format(inputMsg.get("Type").toString()).equals("StartGame")) {
						gameBoard.initialize(inputMsg, clientGameRoom); // Start the game interface

					} else if (format(inputMsg.get("Type").toString()).equals("InGame")) {
						if (format(inputMsg.get("Operation").toString()).equals("SetLetter")) {
							gameBoard.setLetter(); // Activate the crossword form to the player's
													// turn
						} else if (format(inputMsg.get("Operation").toString()).equals("ForVote")) {
							gameBoard.vote(inputMsg); // Vote on all words formed
						} else if (format(inputMsg.get("Operation").toString())
								.equals("UpdateScore")) {
							gameBoard.updateScore(inputMsg); // Update the score of the
																// corresponding player
						} else if (format(inputMsg.get("Operation").toString()).equals("Chat")) {
							gameBoard.displayChatMessage(inputMsg); // Display players' words
						}
					} else if (format(inputMsg.get("Type").toString()).equals("GameOver")) {
						gameBoard.gameOver(); // Game over
					} else if (format(inputMsg.get("Type").toString()).equals("QuitBoard")) {
						gameBoard.gameOver(); // Game over
					} else if (format(inputMsg.get("Type").toString()).equals("GameRoomReset")) {
						clientGameRoom.activateInviteButton();
						if (format(inputMsg.get("CanStart").toString()).equals("Yes")) {
							clientGameRoom.activateStartButton(); // activate the button of room
																	// master
						}
					}
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket closed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}
}
