import java.io.Serializable;

public class User implements Comparable<User>, Serializable {

	private String username;
	private String password;
	private int score;
	private int games;
	private double wins;
	private int lastStreak;
	private int maxStreak;
	private double guessDistribution;
	private boolean online;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.score = 0;
		this.games = 0;
		this.wins = 0.0;
		this.lastStreak = 0;
		this.maxStreak = 0;
		this.guessDistribution = 0.0;
		this.online = false;
	}

	public User() {
		this(null, null);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getScore() {
		return score;
	}

	public void updateScore(int score) {
		this.score += score;
	}

	public int getGames() {
		return games;
	}

	public void incGames() {
		games++;
	}

	public double getWins() {
		return wins;
	}

	public void incWins() {
		wins++;
	}

	public int getLastStreak() {
		return lastStreak;
	}

	public void incLastStreak() {
		lastStreak++;
	}

	public int getMaxStreak() {
		return maxStreak;
	}

	public void incMaxStreak() {
		maxStreak++;
	}

	public double getGuessDistribution() {
		return guessDistribution;
	}

	public void updateGuessDistribution() {

	}

	public boolean isOnline() {
		return online;
	}

	public void online() {
		online = true;
	}

	public void offline() {
		online = false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("username: " + username);
		// builder.append("score: " + score);

		return builder.toString();
	}

	@Override
	public int compareTo(User other) {
		if (username.equals(other.username))
			return 0;

		if (score > other.score)
			return -1;
		else if (score == other.score)
			return username.compareTo(other.username);
		else
			return 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof User)) {
			return false;
		}
		User other = (User) o;
		return username.equals(other.username);
	}

}
