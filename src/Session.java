public class Session {

	private String word;
	private int attempts;
	private boolean close;
	private boolean guessed;

	public Session(String word) {
		this.word = word;
		this.attempts = 0;
		this.close = false;
		this.guessed = false;
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

	public boolean isClose() {
		return this.close;
	}

	public void close() {
		this.close = true;
	}

	public void reset(String word) {
		this.word = word;
		this.attempts = 0;
		this.close = false;
		this.guessed = false;
	}

	public void win() {
		this.guessed = true;
	}

	public void lose() {
		this.guessed = false;
	}

	@Override
	public String toString() {
		return this.word + ", " + this.attempts + " : " + (guessed ? "win" : "lose");
	}
}
