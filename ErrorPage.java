package client;

import javax.swing.JOptionPane;

import com.google.gson.JsonObject;

/**
 * 
 * @author Tianning Sun
 *
 */
public class ErrorPage {
	public void display(JsonObject inputMsg) {
		JOptionPane.showMessageDialog(null, format(inputMsg.get("Detail").toString()), "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public String format(String msg) {
		return msg.substring(1, msg.length() - 1);
	}

}
