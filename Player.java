package server;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 
 * @author Yu Chao
 *
 */
public class Player {

	private String userName;
	private Socket socket;
	private BufferedWriter writer;
	private int room = -1;//In the waitling list
	
	public int getRoom() {
		return room;
	}
	
	public void enterRoom(int i) {
		room = i;
	}
	
	public void quitRoom() {
		room = -1;
	}

	public Player(String userName, Socket socket) {
		this.userName = userName;
		this.socket = socket;
	}

	public String getUsername() {
		return userName;
	}

	public Socket getSocket() {
		return socket;
	}

	public void write(String msg) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			writer.write(msg + "\n");
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
