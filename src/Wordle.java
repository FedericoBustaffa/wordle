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
	ConcurrentHashMap<String, Boolean> sessions;

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

		sessions = new ConcurrentHashMap<String, Boolean>();
	}

	public ConcurrentHashMap<String, Boolean> getSessions() {
		return sessions;
	}

	public void extractWord() {
		List<String> keys = Collections.list(sessions.keys());
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			if (sessions.get(it.next()) == true)
				it.remove();
		}

		Random random = new Random();
		this.current_word = words.remove(random.nextInt(words.size()));
		System.out.println("< extracted word: " + current_word);
	}

	public boolean startSession(String username) {
		return sessions.putIfAbsent(username, false) == null;
	}

	public boolean endSession(String username) {
		return sessions.put(username, true) != null;
	}

	public String guess(String word) {
		if (word.length() != 10)
			return "ERROR: word length must be 10";
		else if (!word.equals(current_word))
			return "wrong word, try again";
		else
			return "you guess right!";
	}

}
