public class Extractor implements Runnable {

	private Wordle wordle;

	public Extractor(Wordle wordle) {
		this.wordle = wordle;
	}

	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				Thread.sleep(5000);
				wordle.extractWord();
			}
		} catch (InterruptedException e) {
			System.out.println("< extractor closure");
		}
	}

}
