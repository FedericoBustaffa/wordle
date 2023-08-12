public class Extractor implements Runnable {

	private Wordle wordle;

	public Extractor(Wordle wordle) {
		this.wordle = wordle;
	}

	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				wordle.extractWord();
				Thread.sleep(30000);
			}
		} catch (InterruptedException e) {
			System.out.println("< extractor closure");
		}
	}

}
