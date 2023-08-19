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
	ConcurrentHashMap<String, String> sessions;
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

		sessions = new ConcurrentHashMap<String, String>();
		random = new Random();
	}

	public ConcurrentHashMap<String, String> getSessions() {
		return sessions;
	}

	public void extractWord() {
		current_word = words.remove(random.nextInt(words.size()));
		System.out.println("< extracted word: " + current_word);
	}

	public boolean startSession(String username) {
		String session_word = sessions.get(username);
		if (session_word != null)
			return false;
		else {
			sessions.put(username, current_word);
			return true;
		}
	}

	public void endSession(String username) {
		String session_word = sessions.get(username);
		if (session_word == null)
			return;
		if (session_word.equals(current_word))
			sessions.put(username, "");
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
			if (sessions.get(username).equals("")) {
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
		String session_word = sessions.get(username);
		// System.out.println("< GUESS: " + word + " : " + session_word);
		if (!sessions.containsKey(username))
			return "ERROR: you have to start a new game before";
		else if (session_word.equals(""))
			return "ERROR: your session is closed";
		else if (word.length() != 10)
			return "ERROR: word length must be 10";
		else if (!word.equals(session_word))
			return hints(word, session_word);
		else
			return "you guess right!";
	}

}
