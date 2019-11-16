package client;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JTextField;

import com.google.gson.JsonObject;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Container;
/**
 * 
 * @author Weiguang Ma, Tianning Sun, Zihe Han
 *
 */
public class ClientRegister {

	private JFrame frame;
	private JTextField userNameInfo;
	private BufferedWriter writer;
	private JsonObject message = new JsonObject();
	private String jsonString;

	public ClientRegister(BufferedWriter writer) {
		this.writer = writer;
	}

	/**
	 * Initialize the contents of the frame.
	 * @wbp.parser.entryPoint
	 */
	public void initialize() {	
		frame = new JFrame("Register");	
		ImageIcon img=new ImageIcon("background/Registerbg.jpg");
		JLabel imgLabel=new JLabel(img);
		frame.getLayeredPane().add(imgLabel,new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0,0,img.getIconWidth(),img.getIconHeight());
		Container cp=frame.getContentPane();	
		cp.setLayout(null);
		JLabel lblNewLabel = new JLabel("User Name");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblNewLabel.setBounds(16, 120, 130, 58);
		cp.add(lblNewLabel);

		userNameInfo = new JTextField();
		userNameInfo.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		userNameInfo.setBounds(139, 125, 255, 50);
		cp.add(userNameInfo);
		userNameInfo.setColumns(10);
		
		JButton submitButton = new JButton("");
		submitButton.setIcon(new ImageIcon("buttonICON/submit.jpg"));	

		// Player enters username
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String content = userNameInfo.getText();
					if(content.equals("")) {
						JOptionPane.showMessageDialog(null,
								"You have to input a player name", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					else if (!Pattern.matches("^[0-9a-zA-Z]+$", content)) {
						JOptionPane.showMessageDialog(null,
								"The username contains illegal characters", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						message = new JsonObject();
						message.addProperty("Direction", "ClientToServer");
						message.addProperty("Function", "Register");
						message.addProperty("Username", userNameInfo.getText());
							         
						jsonString = message.toString();		   
						writer.write(jsonString +"\n");
						writer.flush();					

					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});

		submitButton.setBounds(422, 125, 50, 50);
		cp.add(submitButton);
		
         ((JPanel)cp).setOpaque(false);
		
		frame.setBounds(350, 250, img.getIconWidth(),img.getIconHeight()+20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);		
	}

	// Interface closed after registration
	public void close() {
		frame.dispose();
	}

	public void resetUsername() {
		userNameInfo.setText("");
	}

}
