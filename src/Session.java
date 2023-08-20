public class Session {

	private String word;
	private int attempts;

	public Session(String word) {
		this.word = word;
		attempts = 0;
	}

	public String getWord() {
		return this.word;
	}

	public void close() {
		this.word = "";
	}

	public boolean isClose() {
		return this.word.equals("");
	}

	public int getAttempts() {
		return this.attempts;
	}

	public void increaseAttempts() {
		this.attempts++;
	}

	@Override
	public String toString() {
		if (this.word.equals(""))
			return Integer.toString(attempts);
		else
			return this.word + ", " + this.attempts;
	}
}
