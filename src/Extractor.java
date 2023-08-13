public class Extractor implements Runnable {

	private Wordle wordle;
	private long timeout;

	public Extractor(Wordle wordle, long timeout) {
		this.wordle = wordle;
		this.timeout = timeout;
	}

	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				wordle.extractWord();
				Thread.sleep(timeout);
			}
		} catch (InterruptedException e) {
			System.out.println("< extractor closure");
		}
	}

}
