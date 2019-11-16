package server;
import java.net.Socket;
import java.util.ArrayList;
/**
 * 
 * @author Yu Chao
 *
 */
public class AllPlayer {

	//This class defines two different types of player set
	private ArrayList<Player> player = new ArrayList<Player>(); 
	private ArrayList<Player> waitingPlayer = new ArrayList<Player>(); 
	
	public ArrayList<Player> getWaitingPlayer() {
		return waitingPlayer;
	}
	
	public ArrayList<Player> getPlayer() {
		return player;
	}

	public Player searchPlayerBasedOnName(String userName) {
		for (int i = 0; i < player.size(); i++) {
			if (player.get(i).getUsername().equals(userName))
				return player.get(i);
		}
		return null;
	}
	
	public Player searchPlayerBasedOnSocket(Socket socket) {
		for (int i = 0; i < player.size(); i++) {
			if (player.get(i).getSocket().equals(socket))
				return player.get(i);
		}
		return null;
	}
	
	public void enterWaitingList(Player newplayer) {
		waitingPlayer.add(newplayer);
		newplayer.quitRoom();
	}
	
	public void quitWaitingList(Player newplayer) {
		waitingPlayer.remove(newplayer);
	}

	public void addNewPlayer(Player newplayer) {
		player.add(newplayer);
		enterWaitingList(newplayer);
	}

	public void quitSystem(Player p) {
		player.remove(p);
		quitWaitingList(p);
	}

}
