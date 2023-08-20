import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User implements Comparable<User>, Serializable {

	private String username;
	private String password;
	private int score;
	private int games;
	private int wins;
	private int lastStreak;
	private int maxStreak;
	private double guessDistribution;
	private boolean online;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.score = 0;
		this.games = 0;
		this.wins = 0;
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

	@JsonIgnore
	public double getWinPercentage() {
		return (double) wins / games * 100;
	}

	public int getWins() {
		return wins;
	}

	public void incWins() {
		wins++;
		lastStreak++;
		if (lastStreak > maxStreak)
			maxStreak = lastStreak;
	}

	public int getLastStreak() {
		return lastStreak;
	}

	public void resetLastStreak() {
		lastStreak = 0;
	}

	public int getMaxStreak() {
		return maxStreak;
	}

	public double getGuessDistribution() {
		return guessDistribution;
	}

	public void updateGuessDistribution() {
		guessDistribution = (double) score / wins;
	}

	public String statistics() {
		StringBuilder builder = new StringBuilder();
		builder.append("score: " + score + "\n< ");
		builder.append("games: " + games + "\n< ");
		builder.append("wins: " + String.format("%.2f", getWinPercentage()) + "%\n< ");
		builder.append("last streak: " + lastStreak + "\n< ");
		builder.append("max streak: " + maxStreak + "\n< ");
		builder.append("guess distribution: " + String.format("%.2f", guessDistribution));

		return builder.toString();
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
