package client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;

import com.google.gson.JsonObject;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;
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
public class ClientRoomList {

	private JFrame frame;
	private JTextField textField; // Display current player
	private BufferedWriter writer;
	private JTextArea textArea; // Display room information
	private int RoomCount;
	private JTextField welcomeMessage;
	private JsonObject message = new JsonObject();
	private String jsonString;

	/**
	 * Create the application.
	 */
	public ClientRoomList(BufferedWriter writer) {
		this.writer = writer;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {
		frame = new JFrame("Room List");

		ImageIcon img = new ImageIcon("background/Roomlistbg.jpg");
		JLabel imgLabel = new JLabel(img);
		frame.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
		Container cp = frame.getContentPane();
		cp.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		cp.setLayout(null);
		((JPanel) cp).setOpaque(false);
		frame.setBounds(200, 100, img.getIconWidth(), img.getIconHeight() + 20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel roomList = new JLabel("Room List");
		roomList.setForeground(Color.WHITE);
		roomList.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		roomList.setBounds(350, 8, 141, 39);
		cp.add(roomList);

		textArea = new JTextArea();
		textArea.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		textArea.setEditable(false);

		JScrollPane jsp = new JScrollPane(textArea);
		cp.add(jsp);
		jsp.setBounds(10, 60, 842, 387);

		JLabel selectRoom = new JLabel("Select Room:");
		selectRoom.setForeground(Color.WHITE);
		selectRoom.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		selectRoom.setBounds(10, 487, 172, 29);
		cp.add(selectRoom);

		textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		textField.setBounds(181, 487, 93, 36);
		cp.add(textField);
		textField.setColumns(10);

		JButton enterButton = new JButton("");
		enterButton.setIcon(new ImageIcon("buttonICON/enter.jpg"));

		// Player enters the game room
		enterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String content = textField.getText();
					if (content.equals("")) {
						JOptionPane.showMessageDialog(null, "You have to enter a room number",
								"Error", JOptionPane.ERROR_MESSAGE);
					} else if (!Pattern.matches("^[0-9]+$", content)
							|| Integer.parseInt(content) > RoomCount) {
						JOptionPane.showMessageDialog(null, "Invaild room number", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						message = new JsonObject();
						message.addProperty("Direction", "ClientToServer");
						message.addProperty("Function", "RequestForRoomDetail");
						message.addProperty("RoomNumber", content);
						jsonString = message.toString();
						writer.write(jsonString + "\n");
						writer.flush();
						textField.setText("");
					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		enterButton.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		enterButton.setBounds(390, 475, 50, 50);
		cp.add(enterButton);

		welcomeMessage = new JTextField();
		welcomeMessage.setFont(new Font("Dialog", Font.BOLD, 15));
		welcomeMessage.setEditable(false);
		welcomeMessage.setBounds(10, 6, 286, 29);
		cp.add(welcomeMessage);
		welcomeMessage.setColumns(10);

		JLabel lblEnter = new JLabel("enter=>");
		lblEnter.setForeground(Color.WHITE);
		lblEnter.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblEnter.setBounds(297, 487, 172, 29);
		cp.add(lblEnter);

		// Respond to close button events
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					message = new JsonObject();
					message.addProperty("Direction", "ClientToServer");
					message.addProperty("Function", "QuitRequest");
					message.addProperty("Layer", "System");
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

	// Display all room information
	public void displayInfo(JsonObject inputMsg) {
		welcomeMessage.setText("Hi " + format(inputMsg.get("Destination").toString())
				+ "! Welcome to the Scrabblet!");
		RoomCount = inputMsg.getAsJsonArray("RoomList").size();
		textArea.setText("");
		for (int i = 0; i < RoomCount; i++) {
			textArea.append("Room Number: " + format(inputMsg.getAsJsonArray("RoomList").get(i)
					.getAsJsonObject().get("RoomNumber").toString()) + "\t");
			textArea.append("Room Status: " + format(inputMsg.getAsJsonArray("RoomList").get(i)
					.getAsJsonObject().get("RoomStatus").toString()) + "\t");
			textArea.append("Room Player Number: " + format(inputMsg.getAsJsonArray("RoomList")
					.get(i).getAsJsonObject().get("PlayerNumber").toString()) + "\t");
			textArea.append("Room Master: " + format(inputMsg.getAsJsonArray("RoomList").get(i)
					.getAsJsonObject().get("RoomMaster").toString()) + "\n");
		}
	}

	// Display invitation information
	public void displayInvitation(JsonObject inputMsg) {
		int isAccept = JOptionPane.showConfirmDialog(frame,
				format(inputMsg.get("Message").toString()), "Invition Message",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		try {
			if (isAccept == JOptionPane.YES_OPTION) {
				message = new JsonObject();
				message.addProperty("Direction", "ClientToServer");
				message.addProperty("Function", "RequestForRoomDetail");
				message.addProperty("RoomNumber", format(inputMsg.get("RoomNumber").toString()));
				jsonString = message.toString();
				writer.write(jsonString + "\n");
				writer.flush();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public void resetRoomNumber() {
		textField.setText("");
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}

	public void setInvisiable() {
		frame.setVisible(false);
	}

	public void setVisiable() {
		frame.setVisible(true);
	}
}
