package client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.JTextField;

import com.google.gson.JsonObject;

import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Container;

import javax.swing.ImageIcon;

/**
 * 
 * @author Weiguang Ma, Tianning Sun, Zihe Han
 *
 */
public class ClientGameRoom {

	private JFrame frame;
	private JTextField roomNumberField; // Display the room number
	private int MAXPLAYER = 6; // Maximum player is 6
	private JTextField[] playerSeat = new JTextField[MAXPLAYER]; // Display players in the room
	private JButton startGameButton;
	private JButton inviteButton;
	private BufferedWriter writer;
	private JTextArea textArea; // Display lobby player list
	private JTextField inviteName;
	private JsonObject message;
	private String jsonString;
	private JTextField playerName;

	/**
	 * Create the application.
	 */
	public ClientGameRoom(BufferedWriter writer) {
		this.writer = writer;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {

		frame = new JFrame("Game Room");
		ImageIcon img = new ImageIcon("background/GameRoombg.jpg");
		JLabel imgLabel = new JLabel(img);
		frame.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
		Container cp = frame.getContentPane();
		cp.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		cp.setLayout(null);
		((JPanel) cp).setOpaque(false);
		frame.setBounds(100, 100, img.getIconWidth(), img.getIconHeight() + 20);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JLabel lblNewLabel = new JLabel("Room Number :");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblNewLabel.setBounds(12, 0, 195, 30);
		cp.add(lblNewLabel);

		roomNumberField = new JTextField();
		roomNumberField.setFont(new Font("Dialog", Font.BOLD, 18));
		roomNumberField.setEditable(false);
		roomNumberField.setBounds(227, 6, 48, 25);
		cp.add(roomNumberField);
		roomNumberField.setColumns(10);

		JLabel lblPlayerList = new JLabel("Waiting Player List");
		lblPlayerList.setForeground(Color.WHITE);
		lblPlayerList.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblPlayerList.setBounds(486, 0, 194, 30);
		cp.add(lblPlayerList);

		textArea = new JTextArea();
		textArea.setBackground(Color.WHITE);
		textArea.setFont(new Font("Dialog", Font.ITALIC, 18));
		textArea.setEditable(false);
		textArea.setBounds(474, 34, 217, 395);
		cp.add(textArea);

		inviteButton = new JButton("");
		inviteButton.setIcon(new ImageIcon("buttonICON/invite.jpg"));

		// Invite players to join the room
		inviteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					message = new JsonObject();
					if (inviteName.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Please input a playername", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else if (!Pattern.matches("^[0-9a-zA-Z]+$", inviteName.getText())) {
						JOptionPane.showMessageDialog(null, "Invalid playername format", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						message.addProperty("Direction", "ClientToServer");
						message.addProperty("Function", "Invitation");
						message.addProperty("Player", inviteName.getText());
						message.addProperty("RoomNumber", roomNumberField.getText());

						jsonString = message.toString();
						writer.write(jsonString + "\n");
						writer.flush();
					}

				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Interrupted connection to server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		inviteButton.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		inviteButton.setBounds(646, 453, 45, 45);
		inviteButton.setEnabled(false);
		cp.add(inviteButton);

		for (int i = 0; i < playerSeat.length; i++) {
			playerSeat[i] = new JTextField();
			playerSeat[i].setColumns(10);
			playerSeat[i].setBounds(10, 100 + 60 * i, 380, 30);
			playerSeat[i].setEditable(false);
			cp.add(playerSeat[i]);
		}

		startGameButton = new JButton("");
		startGameButton.setIcon(new ImageIcon("buttonICON/startgame.jpg"));
		startGameButton.setEnabled(false);

		// The room master can start the game
		startGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					message = new JsonObject();
					message.addProperty("Direction", "ClientToServer");
					message.addProperty("Function", "StartGame");
					message.addProperty("RoomNumber", roomNumberField.getText());
					jsonString = message.toString();
					writer.write(jsonString + "\n");
					writer.flush();
					startGameButton.setEnabled(false);
					inviteButton.setEnabled(false);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		startGameButton.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		startGameButton.setBounds(201, 453, 50, 50);
		cp.add(startGameButton);

		JLabel lblNewLabel_1 = new JLabel("Players :");
		lblNewLabel_1.setForeground(Color.WHITE);
		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblNewLabel_1.setBounds(12, 75, 136, 26);
		cp.add(lblNewLabel_1);

		inviteName = new JTextField();
		inviteName.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		inviteName.setBounds(474, 454, 150, 44);
		cp.add(inviteName);
		inviteName.setColumns(10);
		inviteName.setEnabled(false);

		JLabel lblNewLabel_2 = new JLabel("Current Player : ");
		lblNewLabel_2.setForeground(Color.WHITE);
		lblNewLabel_2.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblNewLabel_2.setBounds(12, 37, 206, 25);
		cp.add(lblNewLabel_2);

		playerName = new JTextField();
		playerName.setFont(new Font("Dialog", Font.BOLD, 18));
		playerName.setEditable(false);
		playerName.setBounds(226, 36, 224, 36);
		cp.add(playerName);
		playerName.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("Start Game=>");
		lblNewLabel_3.setForeground(Color.WHITE);
		lblNewLabel_3.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblNewLabel_3.setBounds(41, 455, 159, 36);
		cp.add(lblNewLabel_3);

		frame.setVisible(true);

		// Respond to close button events
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					message = new JsonObject();
					message.addProperty("Direction", "ClientToServer");
					message.addProperty("Function", "QuitRequest");
					message.addProperty("Layer", "Room");
					message.addProperty("RoomNumber", roomNumberField.getText());
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
	}

	// Display game room information
	public void display(JsonObject inputMsg) {
		roomNumberField.setText(format(inputMsg.get("RoomNumber").toString()));

		playerName.setText(format(inputMsg.get("Destination").toString()));

		startGameButton.setEnabled(false);
		for (int i = 0; i < 6; i++) {
			playerSeat[i].setText("");
		}
		for (int i = 0; i < inputMsg.getAsJsonArray("PlayerList").size(); i++) {
			playerSeat[i].setText(format(inputMsg.getAsJsonArray("PlayerList").get(i)
					.getAsJsonObject().get("PlayerName").toString()));
		}

		if (format(inputMsg.getAsJsonArray("PlayerList").get(0).getAsJsonObject().get("PlayerName")
				.toString()).equals(format(inputMsg.get("Destination").toString()))) { // Only the
																						// room
																						// master
																						// can start
																						// the game
			if (inputMsg.getAsJsonArray("PlayerList").size() > 1) { // At least two people can start
																	// the game
				startGameButton.setEnabled(true);
			}
			inviteButton.setEnabled(true);
			inviteName.setEnabled(true);
		}
	}

	// Players in the game lobby are displayed in the game room
	public void displayWaitingList(JsonObject inputMsg) {
		textArea.setText("");
		for (int i = 0; i < inputMsg.getAsJsonArray("WaitingList").size(); i++) {
			textArea.append(format(inputMsg.getAsJsonArray("WaitingList").get(i).getAsJsonObject()
					.get("PlayerName").toString()) + "\n");
		}
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}

	public void resetInvitedPlayer() {
		inviteName.setText("");
	}

	public void invitationACK() {
		JOptionPane.showMessageDialog(frame, "You have invited player " + inviteName.getText(),
				null, JOptionPane.INFORMATION_MESSAGE);
		resetInvitedPlayer();
	}

	public void activateStartButton() {
		startGameButton.setEnabled(true);
	}

	public void activateInviteButton() {
		inviteButton.setEnabled(true);
	}

	public void setInvisiable() {
		frame.setVisible(false);
	}

	public void setVisiable() {
		frame.setVisible(true);
	}
}
