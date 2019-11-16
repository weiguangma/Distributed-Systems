package server;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JPanel;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Container;

/**
 * 
 * @author Zihe Han
 *
 */
public class ServerWindow {

	JFrame frame;
	JTextArea textArea;
	JTextField textField;

	/**
	 * Create the application.
	 */
	public ServerWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("Server");

		ImageIcon img = new ImageIcon("background/Serverbg.jpg");
		JLabel imgLabel = new JLabel(img);
		frame.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
		Container cp = frame.getContentPane();
		cp.setLayout(null);
		((JPanel) cp).setOpaque(false);
		frame.setBounds(100, 100, 600, img.getIconHeight());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel lblMessage = new JLabel("server");
		lblMessage.setForeground(Color.WHITE);
		lblMessage.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblMessage.setBounds(23, 6, 70, 20);
		cp.add(lblMessage);

		textArea = new JTextArea();
		textArea.setBackground(Color.WHITE);
		textArea.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		textArea.setBounds(60, 70, 400, 300);

		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setBounds(60, 70, 500, 350);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		cp.add(scroll);

		textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		textField.setBounds(490, 10, 70, 38);
		textField.setEditable(false);
		textField.setText("0");
		cp.add(textField);
		textField.setColumns(10);

		JLabel lblConnectionNumber = new JLabel("Connection number :");
		lblConnectionNumber.setForeground(Color.WHITE);
		lblConnectionNumber.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		lblConnectionNumber.setBounds(312, 15, 166, 33);
		cp.add(lblConnectionNumber);

	}
}
