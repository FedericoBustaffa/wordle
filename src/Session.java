public class Session {

	private String word;
	private int attempts;
	private boolean close;

	public Session(String word) {
		this.word = word;
		this.attempts = 0;
		this.close = false;
	}

	public String getWord() {
		return this.word;
	}

	public void close() {
		this.close = true;
	}

	public boolean isClose() {
		return this.close;
	}

	public int getAttempts() {
		return this.attempts;
	}

	public void increaseAttempts() {
		this.attempts++;
	}

	public void reset(String word) {
		this.word = word;
		this.attempts = 0;
		this.close = false;
	}

	@Override
	public String toString() {
		return this.word + ", " + this.attempts;
	}
}
