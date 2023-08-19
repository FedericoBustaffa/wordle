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

	public int getAttempts() {
		return this.attempts;
	}

	public void increaseAttempts() {
		this.attempts++;
	}

}
