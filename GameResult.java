package client;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JTextField;

import com.google.gson.JsonObject;

import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Container;
/**
 * 
 * @author Weiguang Ma, Yu Chao, Zihe Han
 *
 */
public class GameResult {

	private JFrame frame;
	private JTextField winnerTextField;
	private JsonObject message;
	private String jsonString;
	private BufferedWriter writer;

	/**
	 * Initialize the contents of the frame.
	 */
	
	public GameResult(BufferedWriter writer) {
		this.writer = writer;
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialize(String winner,String list, String roomNumber, ClientGameRoom cgr) {
		frame = new JFrame("Game Result");
		ImageIcon img=new ImageIcon("background/Gameresultbg.jpg");
		JLabel imgLabel=new JLabel(img);
		frame.getLayeredPane().add(imgLabel,new Integer(Integer.MIN_VALUE));
		imgLabel.setBounds(0,0,img.getIconWidth(),img.getIconHeight());
		Container cp=frame.getContentPane();
		cp.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		cp.setLayout(null);
		((JPanel)cp).setOpaque(false);
		frame.setBounds(100, 100, img.getIconWidth(),img.getIconHeight()+20);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel lblGameOver = new JLabel("GAME OVER");
		lblGameOver.setForeground(Color.WHITE);
		lblGameOver.setBackground(Color.WHITE);
		lblGameOver.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblGameOver.setBounds(212, 6, 156, 42);
		cp.add(lblGameOver);
		
		JLabel lblWinner = new JLabel("Winner :");
		lblWinner.setForeground(Color.WHITE);
		lblWinner.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblWinner.setBounds(12, 57, 113, 31);
		cp.add(lblWinner);
		
		winnerTextField = new JTextField();
		winnerTextField.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		winnerTextField.setEditable(false);
		winnerTextField.setBounds(126, 57, 400, 31);
		cp.add(winnerTextField);
		winnerTextField.setColumns(10);

		winnerTextField.setText(winner);
		
		JLabel lblRankList = new JLabel("Rank List");
		lblRankList.setForeground(Color.WHITE);
		lblRankList.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblRankList.setBounds(223, 100, 156, 31);
		cp.add(lblRankList);
		
		JTextArea rankListTextArea = new JTextArea();
		rankListTextArea.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		rankListTextArea.setEditable(false);
		rankListTextArea.setBounds(83, 131, 400, 267);
		cp.add(rankListTextArea);
		
		rankListTextArea.append(list);
		
		// Respond to close button events
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					message = new JsonObject();
					message.addProperty("Direction", "ClientToServer");
					message.addProperty("Function", "GameOverACK");
					message.addProperty("RoomNumber",roomNumber);
					jsonString = message.toString();		   
					writer.write(jsonString +"\n");
					writer.flush();
					cgr.setVisiable();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Fail to connect Server", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
		
		frame.setVisible(true);
		
	}
}
