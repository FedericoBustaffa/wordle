import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

public class Wordle {

	String current_word;
	List<String> words;
	ConcurrentHashMap<String, String> sessions;

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
	}

	public ConcurrentHashMap<String, String> getSessions() {
		return sessions;
	}

	public void extractWord() {
		Random random = new Random();
		this.current_word = words.remove(random.nextInt(words.size()));
		System.out.println("< extracted word: " + current_word);
	}

	public boolean startSession(String username) {
		return sessions.put(username, this.current_word) == null;
	}

	public boolean endSession(String username) {
		return sessions.put(username, "") != null;
	}

	public void clear() {
		List<String> keys = Collections.list(sessions.keys());
		Iterator<String> it = keys.iterator();
		String username;
		while (it.hasNext()) {
			username = it.next();
			System.out.println("< " + sessions.get(username).equals(""));
			if (sessions.get(username).equals("")) {
				sessions.remove(username);
				System.out.println("< " + sessions);
			}
		}
	}

	public String guess(String username, String word) {
		String session_word = sessions.get(username);
		if (!sessions.containsKey(username))
			return "ERROR: you have to start a new game before";
		else if (sessions.containsKey(username) && session_word.equals(""))
			return "ERROR: your session is closed";
		else if (word.length() != 10)
			return "ERROR: word length must be 10";
		else if (!word.equals(session_word))
			return "wrong word, try again";
		else {
			if (!session_word.equals(current_word)) {
				sessions.remove(username);
				System.out.println("< " + sessions);
			}
			return "you guess right!";
		}
	}

}
