import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Iterator;

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
			while (scanner.hasNext()) {
				words.add(scanner.nextLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		sessions = new ConcurrentHashMap<String, Session>();
		random = new Random();
	}

	public ConcurrentHashMap<String, Session> getSessions() {
		return sessions;
	}

	public void extractWord() {
		current_word = words.remove(random.nextInt(words.size()));
		System.out.println("< extracted word: " + current_word);
	}

	public boolean startSession(String username) {
		Session session = sessions.get(username);
		if (session != null)
			return false;
		else {
			sessions.put(username, new Session(current_word));
			return true;
		}
	}

	public void endSession(String username) {
		Session session = sessions.get(username);
		if (session == null)
			return;
		else if (session.getWord().equals(current_word))
			session.close();
		else
			sessions.remove(username);
	}

	public void clear() {
		List<String> keys = Collections.list(sessions.keys());
		Iterator<String> it = keys.iterator();
		String username;
		while (it.hasNext()) {
			username = it.next();
			// System.out.println("< " + sessions.get(username).equals(""));
			if (sessions.get(username).getWord().equals("")) {
				sessions.remove(username);
				System.out.println("< " + sessions);
			}
		}
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
		// System.out.println("< GUESS: " + word + " : " + session_word);
		if (!sessions.containsKey(username))
			return "ERROR: you have to start a new game before";
		else if (session.getWord().equals(""))
			return "ERROR: your session is closed";
		else if (word.length() != 10)
			return "ERROR: word length must be 10";
		else if (session.getAttempts() >= 2) {
			return "ERROR: attempts terminated";
		} else if (!word.equals(session.getWord())) {
			String msg = hints(word, session.getWord());
			session.increaseAttempts();
			if (session.getAttempts() >= 2)
				session.close();
			return msg;
		} else {
			session.increaseAttempts();
			if (session.getAttempts() >= 2)
				session.close();
			return "you guess right!";
		}
	}

}
