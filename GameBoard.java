package client;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Container;

/**
 * 
 * @author Weiguang Ma, Tianning Sun, Zihe Han
 *
 */
public class GameBoard {

	private JFrame frame;
	private int ROW = 20;
	private int COLUME = 20;
	private JTextField[][] board = new JTextField[ROW][COLUME];
	private JTextField[] scoreBoard = new JTextField[6];
	private JTextField textField;
	private JTextArea textArea;
	private BufferedWriter writer;
	private String roomNumber;
	private ClientGameRoom gameRoom;
	private ArrayList<GamePlayer> playerList = new ArrayList<GamePlayer>();
	private GamePlayer gamePlayer;
	private JButton confirmButton;
	private JButton passButton;
	private int temprow = 0;
	private int tempcol = 0;
	private JsonObject message;
	private String jsonString;
	private JTextField currentPlayer;
	private JsonArray gameWord = new JsonArray();
	private GameResult gameResult;
	private JLabel lblComfirm;
	private JLabel lblPass;

	/**
	 * Create the application.
	 */
	public GameBoard(BufferedWriter writer) {
		this.writer = writer;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void initialize(JsonObject inputMsg, ClientGameRoom cgr) {
		gameRoom = cgr;

		roomNumber = format(inputMsg.get("RoomNumber").toString());

		frame = new JFrame("Game Board");
		ImageIcon img = new ImageIcon("background/Gameboardbg.jpg");
		JLabel imgLabel = new JLabel(img);
		frame.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
		Container cp = frame.getContentPane();
		cp.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		cp.setLayout(null);
		((JPanel) cp).setOpaque(false);
		frame.setBounds(100, 100, img.getIconWidth(), img.getIconHeight() + 20);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JLabel lblScoreBoard = new JLabel("Score board");
		lblScoreBoard.setForeground(Color.WHITE);
		lblScoreBoard.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblScoreBoard.setBounds(898, 13, 162, 31);
		cp.add(lblScoreBoard);

		JLabel lblChatArea = new JLabel("Chat Area");
		lblChatArea.setForeground(Color.WHITE);
		lblChatArea.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblChatArea.setBounds(896, 397, 154, 31);
		cp.add(lblChatArea);

		textArea = new JTextArea();
		textArea.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		textArea.setLineWrap(true);
		textArea.setEditable(false);

		JScrollPane jsp = new JScrollPane(textArea);
		cp.add(jsp);
		jsp.setBounds(750, 432, 424, 291);

		textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		textField.setBounds(750, 736, 344, 39);
		cp.add(textField);
		textField.setColumns(10);

		JButton submitButton = new JButton("");
		submitButton.setIcon(new ImageIcon("buttonICON/chat.jpg"));
		submitButton.setFont(new Font("Lucida Grande", Font.PLAIN, 15));

		// Player input chat content
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chatContent = textField.getText();
				message = new JsonObject();
				message.addProperty("Direction", "ClientToServer");
				message.addProperty("Function", "InGame");
				message.addProperty("RoomNumber", roomNumber);
				message.addProperty("Operation", "Chat");
				message.addProperty("Speaker", currentPlayer.getText());
				message.addProperty("ChatContent", chatContent);

				jsonString = message.toString();
				try {
					writer.write(jsonString + "\n");
					writer.flush();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				textField.setText("");
			}
		});
		submitButton.setBounds(1124, 735, 44, 40);
		cp.add(submitButton);

		passButton = new JButton("");
		passButton.setIcon(new ImageIcon("buttonICON/pass.jpg"));
		passButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				for (int i = 0; i < ROW; i++) {
					for (int j = 0; j < COLUME; j++) {
						if (board[i][j].isEditable()) {
							board[i][j].setText("");
							board[i][j].setEditable(false);
						}
					}
				}

				message = new JsonObject();
				message.addProperty("Direction", "ClientToServer");
				message.addProperty("Function", "InGame");
				message.addProperty("RoomNumber", roomNumber);
				message.addProperty("PassPlayer", currentPlayer.getText());
				message.addProperty("Operation", "Pass");

				jsonString = message.toString();
				try {
					writer.write(jsonString + "\n");
					writer.flush();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}

				confirmButton.setEnabled(false);
				passButton.setEnabled(false);
			}
		});
		passButton.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		passButton.setBounds(396, 703, 60, 60);
		passButton.setEnabled(false);
		cp.add(passButton);

		confirmButton = new JButton("");
		confirmButton.setIcon(new ImageIcon("buttonICON/comfirm.jpg"));
		confirmButton.setFont(new Font("Lucida Grande", Font.BOLD, 15));

		// Player set a letter
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int change = 0;
					for (int i = 0; i < ROW; i++) {
						for (int j = 0; j < COLUME; j++) {
							if (!board[i][j].getText().equals("") && board[i][j].isEditable()) {
								change++;
								temprow = i;
								tempcol = j;
							}
						}
					}

					// Players are only allowed to set one letter at a time
					if (change == 1
							&& Pattern.matches("^[a-zA-Z]$", board[temprow][tempcol].getText())) {
						message = new JsonObject();
						message.addProperty("Direction", "ClientToServer");
						message.addProperty("Function", "InGame");
						message.addProperty("RoomNumber", roomNumber);
						message.addProperty("Operation", "ForVote");
						message.addProperty("Row", temprow + "");
						message.addProperty("Column", tempcol + "");
						message.addProperty("Letter", board[temprow][tempcol].getText());

						jsonString = message.toString();
						writer.write(jsonString + "\n");
						writer.flush();

						for (int i = 0; i < ROW; i++) {
							for (int j = 0; j < COLUME; j++) {
								board[i][j].setEditable(false);
							}
						}
						confirmButton.setEnabled(false);
						passButton.setEnabled(false);
					} else {
						JOptionPane.showMessageDialog(null, "You should only input 1 letter",
								"Input ERROR", JOptionPane.INFORMATION_MESSAGE);
					}

				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		confirmButton.setBounds(166, 703, 60, 60);
		confirmButton.setEnabled(false);
		cp.add(confirmButton);

		JLabel lblPlayer = new JLabel("Player : ");
		lblPlayer.setForeground(Color.WHITE);
		lblPlayer.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblPlayer.setBounds(12, 13, 97, 31);
		cp.add(lblPlayer);

		currentPlayer = new JTextField();
		currentPlayer.setEditable(false);
		currentPlayer.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		currentPlayer.setBounds(119, 13, 176, 31);
		cp.add(currentPlayer);
		currentPlayer.setColumns(10);

		// Initialize the crossword form
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COLUME; j++) {
				board[i][j] = new JTextField();
				board[i][j].setBounds(31 * j + 60, 31 * i + 60, 30, 30);
				cp.add(board[i][j]);
				board[i][j].setColumns(10);
				board[i][j].setText("");
				board[i][j].setEditable(false);
			}
		}

		// Initialize the scoreboard
		for (int i = 0; i < scoreBoard.length; i++) {
			scoreBoard[i] = new JTextField();
			scoreBoard[i].setBounds(800, 100 + 50 * i, 300, 40);
			cp.add(scoreBoard[i]);
			scoreBoard[i].setEditable(false);
			scoreBoard[i].setColumns(10);
		}

		playerList = new ArrayList<>(); // Initialize the player list

		for (int i = 0; i < inputMsg.getAsJsonArray("PlayerList").size(); i++) {
			gamePlayer = new GamePlayer();
			gamePlayer.setPlayerName(format(inputMsg.getAsJsonArray("PlayerList").get(i)
					.getAsJsonObject().get("PlayerName").toString()));
			gamePlayer.setPlayerScore(0);
			playerList.add(gamePlayer);

			scoreBoard[i].setText("");
			scoreBoard[i].setText("Player: " + gamePlayer.getPlayerName() + "\t" + "Score: "
					+ gamePlayer.getPlayerScore());
		}

		currentPlayer.setText(format(inputMsg.get("Destination").toString()));

		lblComfirm = new JLabel("comfirm=>");
		lblComfirm.setForeground(Color.WHITE);
		lblComfirm.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblComfirm.setBounds(31, 712, 123, 39);
		cp.add(lblComfirm);

		lblPass = new JLabel("pass=>");
		lblPass.setForeground(Color.WHITE);
		lblPass.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblPass.setBounds(271, 712, 123, 39);
		cp.add(lblPass);

		// Respond to close button events
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					message = new JsonObject();
					message.addProperty("Direction", "ClientToServer");
					message.addProperty("Function", "QuitRequest");
					message.addProperty("Layer", "Board");
					message.addProperty("RoomNumber", roomNumber);
					message.addProperty("QuitPlayer", currentPlayer.getText());

					jsonString = message.toString();
					writer.write(jsonString + "\n");
					writer.flush();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		frame.setVisible(true);
	}

	// Activate the crossword form to the player's turn
	public void setLetter() {
		confirmButton.setEnabled(true);
		passButton.setEnabled(true);
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COLUME; j++) {
				if (board[i][j].getText().equals("")) {
					board[i][j].setEditable(true);
				}
			}
		}
	}

	// Get the word to be voted
	public JsonArray getGameWord(int r, int c) {
		JsonObject wordr = new JsonObject();
		JsonObject wordc = new JsonObject();

		String word[] = new String[2]; // word[0]: Words consisting of consecutive letters in the
										// same row;
										// word[1]: Words consisting of consecutive
										// letters in the same column
		word[0] = board[r][c].getText();
		word[1] = board[r][c].getText();

		// Stitch letters and form words
		for (int i = 1; r + i < board.length; i++) {
			if (board[r + i][c].getText().equals("")) {
				break;
			}
			word[0] = word[0] + board[r + i][c].getText();
		}

		for (int i = 1; r - i >= 0; i++) {
			if (board[r - i][c].getText().equals("")) {
				break;
			}
			word[0] = board[r - i][c].getText() + word[0];
		}

		for (int i = 1; c + i < board.length; i++) {
			if (board[r][c + i].getText().equals("")) {
				break;
			}
			word[1] = word[1] + board[r][c + i].getText();
		}
		for (int i = 1; c - i >= 0; i++) {
			if (board[r][c - i].getText().equals("")) {
				break;
			}
			word[1] = board[r][c - i].getText() + word[1];
		}

		// Only pass the vertical word when there is only one letter
		if (word[0].length() + word[1].length() != 2) {
			wordr.addProperty("Word", word[0]);
			wordr.addProperty("Score", word[0].length());
		}

		wordc.addProperty("Word", word[1]);
		wordc.addProperty("Score", word[1].length());

		gameWord = new JsonArray();

		gameWord.add(wordr);
		gameWord.add(wordc);

		return gameWord;

	}

	// Display the letter filled in by a player
	public void updateBoard(JsonObject inputMsg) {
		board[Integer.parseInt(format(inputMsg.get("Row").toString()))][Integer
				.parseInt(format(inputMsg.get("Column").toString()))]
						.setText(format(inputMsg.get("Letter").toString()));
	}

	// Vote on all words formed
	public void vote(JsonObject inputMsg) {
		message = new JsonObject();
		message.addProperty("Direction", "ClientToServer");
		message.addProperty("Function", "InGame");
		message.addProperty("Operation", "VoteResult");
		message.addProperty("RoomNumber", roomNumber);
		message.addProperty("ScoredPlayer", format(inputMsg.get("ScoredPlayer").toString()));

		int r = Integer.parseInt(format(inputMsg.get("Row").toString()));
		int c = Integer.parseInt(format(inputMsg.get("Column").toString()));
		ArrayList<String> voteWord = new ArrayList<String>();

		String word[] = new String[2];

		board[r][c].setText(format(inputMsg.get("Letter").toString()));

		word[0] = board[r][c].getText();
		word[1] = board[r][c].getText();
		board[r][c].setBackground(Color.YELLOW);

		// Connect letters into a word
		for (int i = 1; r + i < board.length; i++) {
			if (board[r + i][c].getText().equals("")) {
				break;
			}
			word[0] = word[0] + board[r + i][c].getText();
			board[r + i][c].setBackground(Color.YELLOW);
		}

		for (int i = 1; r - i >= 0; i++) {
			if (board[r - i][c].getText().equals("")) {
				break;
			}
			word[0] = board[r - i][c].getText() + word[0];
			board[r - i][c].setBackground(Color.YELLOW);
		}

		for (int i = 1; c + i < board.length; i++) {
			if (board[r][c + i].getText().equals("")) {
				break;
			}
			word[1] = word[1] + board[r][c + i].getText();
			board[r][c + i].setBackground(Color.YELLOW);
		}
		for (int i = 1; c - i >= 0; i++) {
			if (board[r][c - i].getText().equals("")) {
				break;
			}
			word[1] = board[r][c - i].getText() + word[1];
			board[r][c - i].setBackground(Color.YELLOW);
		}

		if (word[0].length() + word[1].length() == 2) {
			voteWord.add(word[0]);
		} else {
			voteWord.add(word[0]);
			voteWord.add(word[1]);
		}
		voteWord(voteWord);
	}

	// Initiate a vote on every word
	public void voteWord(ArrayList<String> voteWord) {

		JsonArray voteword = new JsonArray();
		JsonObject tmp = new JsonObject();

		for (String s : voteWord) {
			tmp = new JsonObject();
			tmp.addProperty("Word", s);
			int isAccept = JOptionPane.showConfirmDialog(null, "Is " + "\"" + s + "\"" + " a word?",
					"Vote", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (isAccept == JOptionPane.YES_OPTION) {
				tmp.addProperty("IsAWord", "Yes");
			} else {
				tmp.addProperty("IsAWord", "No");
			}
			voteword.add(tmp);
		}

		message.add("VoteWord", voteword);

		jsonString = message.toString();

		try {
			writer.write(jsonString + "\n");
			writer.flush();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COLUME; j++) {
				board[i][j].setBackground(Color.WHITE);
			}
		}
	}

	// Update the score of the corresponding player
	public void updateScore(JsonObject inputMsg) {
		String name = format(inputMsg.get("ScoredPlayer").toString());
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).getPlayerName().equals(name)) {
				playerList.get(i).setPlayerScore(playerList.get(i).getPlayerScore()
						+ Integer.parseInt(format(inputMsg.get("Score").toString())));
				scoreBoard[i].setText("Player: " + playerList.get(i).getPlayerName() + "\t"
						+ "Score: " + playerList.get(i).getPlayerScore());
				break;
			}
		}
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}

	// Display the chat message
	public void displayChatMessage(JsonObject inputMsg) {
		textArea.append(format(inputMsg.get("Speaker").toString()) + " said: "
				+ format(inputMsg.get("ChatContent").toString()) + "\n");
	}

	// Display the game result
	public void gameOver() {
		gameResult = new GameResult(writer);

		String winnersName = "";
		String resultList = "";

		GamePlayer[] tempList = new GamePlayer[playerList.size()];
		for (int i = 0; i < tempList.length; i++) {
			tempList[i] = playerList.get(i);
		}

		Arrays.sort(tempList); // Sort the players according to the scores

		int max = tempList[tempList.length - 1].getPlayerScore();
		int position = 0;

		for (int i = tempList.length - 1; i >= 0; i--) {
			if (max == tempList[i].getPlayerScore()) {
				winnersName += tempList[i].getPlayerName() + "\t";
			}
			resultList += "No." + (++position) + "\t" + tempList[i].getPlayerName() + "\t"
					+ tempList[i].getPlayerScore() + "\n";
		}
		gameResult.initialize(winnersName, resultList, roomNumber, gameRoom);
		frame.dispose();
	}

}
