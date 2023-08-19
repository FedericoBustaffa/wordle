import java.io.Serializable;

public class User implements Comparable<User>, Serializable {

	private String username;
	private String password;
	private int score;
	private int games;
	private double wins;
	private int last_streak;
	private int max_streak;
	private double guess_distribution;
	private boolean online;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.score = 0;
		this.games = 0;
		this.wins = 0.0;
		this.last_streak = 0;
		this.max_streak = 0;
		this.guess_distribution = 0.0;
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
		return last_streak;
	}

	public void incLastStreak() {
		last_streak++;
	}

	public int getMaxStreak() {
		return max_streak;
	}

	public void incMaxStreak() {
		max_streak++;
	}

	public double getGuessDistribution() {
		return guess_distribution;
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
