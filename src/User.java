public class User implements Comparable<User> {

	private String username;
	private String password;
	private int score;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.score = 0;
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
	public int compareTo(User other) {
		if (score > other.score)
			return 1;
		else if (score == other.score)
			return 0;
		else
			return -1;
	}

}
