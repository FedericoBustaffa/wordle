public class User implements Comparable<User> {

	private String username;
	private String password;
	private int score;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.score = 0;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("username: " + username + "\n");
		builder.append("score: " + score);

		return builder.toString();
	}

	@Override
	public int compareTo(User other) {
		if (username.equals(other.username))
			return 0;

		if (score > other.score)
			return 1;
		else if (score == other.score)
			return username.compareTo(other.username);
		else
			return -1;
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
