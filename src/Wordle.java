import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Wordle implements Runnable {

	private String current_word;
	private List<String> words;
	private ConcurrentHashMap<String, Session> sessions;
	private long EXTRACTION_TIMEOUT;
	private Random random;

	public Wordle(File file, long EXTRACTION_TIMEOUT) {
		if (!file.exists()) {
			System.out.println("< words file not found");
			System.exit(1);
		}

		current_word = null;
		words = new Vector<String>();
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNext())
				words.add(scanner.nextLine());
		} catch (IOException e) {
			e.printStackTrace();
		}

		sessions = new ConcurrentHashMap<String, Session>();
		this.EXTRACTION_TIMEOUT = EXTRACTION_TIMEOUT;
		random = new Random();
	}

	public Session get(String username) {
		return sessions.get(username);
	}

	public void extractWord() {
		current_word = words.get(random.nextInt(words.size()));
		System.out.println("< extracted word: " + current_word);
	}

	public boolean startSession(String username) {
		Session session = sessions.get(username);
		if (session == null) {

			sessions.put(username, new Session(current_word));
			return true;
		} else if (session.isClose()) {
			if (!current_word.equals(session.getWord())) {
				session.reset(current_word);
				return true;
			}
			return false;
		} else
			return false;
	}

	public void closeSession(String username) {
		Session session = sessions.get(username);
		if (session == null)
			return;
		else
			session.close();
	}

	private String hints(String word, String session_word) {
		StringBuilder builder = new StringBuilder("WORD: " + word);
		builder.append("\n< HINT: ");
		for (int i = 0; i < 10; i++) {
			if (word.charAt(i) == session_word.charAt(i))
				builder.append("+");
			else if (session_word.contains(word.substring(i, i + 1)))
				builder.append("?");
			else
				builder.append("x");
		}

		return builder.toString();
	}

	public String guess(String username, String word) {
		Session session = sessions.get(username);
		if (!sessions.containsKey(username))
			return "ERROR: you have to start a new game before";
		else if (session.getAttempts() >= 12)
			return "ERROR: attempts terminated";
		else if (session.isClose())
			return "ERROR: your session is closed";
		else if (!words.contains(word))
			return "ERROR: invalid word";
		else if (word.length() != 10)
			return "ERROR: word length must be 10";
		else {
			String msg;
			session.increaseAttempts();
			if (word.equals(session.getWord())) {
				msg = "SUCCESS: you guess right: " + session.getWord();
				session.close();
				session.win();
			} else if (session.getAttempts() >= 12) {
				msg = "ERROR: attempts terminated for " + session.getWord();
				session.close();
			} else
				msg = hints(word, session.getWord());

			return msg;
		}
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				extractWord();
				Thread.sleep(EXTRACTION_TIMEOUT);
			}
		} catch (InterruptedException ignored) {
		}
	}

}
