import java.util.Set;
import java.util.TreeSet;

public class Wordle {

	private Set<User> users;

	public Wordle() {
		users = new TreeSet<User>();
	}

	public boolean add(User u) {
		return users.add(u);
	}

	public Set<User> getUsers() {
		return users;
	}

}