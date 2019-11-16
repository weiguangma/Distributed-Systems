package client;

/**
 * 
 * @author Yu Chao
 *
 */
public class GamePlayer implements Comparable<GamePlayer> {

	private String playerName;
	private int playerScore;

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getPlayerScore() {
		return playerScore;
	}

	public void setPlayerScore(int playerScore) {
		this.playerScore = playerScore;
	}

	public int compareTo(GamePlayer gamePlayer) {
		int result = this.playerScore - gamePlayer.playerScore;
		return result;
	}
}
