import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Wordle {

	String current_word;
	List<String> words;
	ConcurrentHashMap<String, Session> sessions;
	Random random;

	public Wordle(File file) {
		if (!file.exists()) {
			System.out.println("< words file not found");
			System.exit(1);
		}

		current_word = null;
		words = new LinkedList<String>();
		try (Scanner scanner = new Scanner(file)) {
			String word;
			while (scanner.hasNext()) {
				word = scanner.nextLine();
				if (word.length() == 10)
					words.add(word);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		sessions = new ConcurrentHashMap<String, Session>();
		random = new Random();
	}

	public void getSessions() {
		System.out.println("< " + sessions);
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
		if (session != null) {
			if (session.isClose()) {
				session.reset(current_word);
				return true;
			} else
				return false;
		} else {
			sessions.put(username, new Session(current_word));
			return true;
		}
	}

	public void endSession(String username) {
		Session session = sessions.get(username);
		if (session == null)
			return;
		else
			session.close();
	}

	private String hints(String word, String session_word) {
		StringBuilder builder = new StringBuilder(word);
		builder.append("\n< ");
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
			if (!word.equals(session.getWord()))
				msg = hints(word, session.getWord());
			else {
				msg = "you guess right: " + session.getWord();
				session.close();
			}

			session.increaseAttempts();
			if (session.getAttempts() >= 12)
				session.close();

			return msg;
		}
	}

}
