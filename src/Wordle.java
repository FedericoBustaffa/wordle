import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Wordle {

	private Set<User> users;
	private Server server;
	private JsonUser json_user;

	public Wordle() {
		users = Collections.synchronizedSet(new TreeSet<User>());
		server = new Server(users);
		json_user = new JsonUser("users.json");
	}

	public void run() {
		while (true)
			server.multiplex();
	}

	public boolean add(User u) {
		return users.add(u);
	}

	public Set<User> getUsers() {
		return users;
	}

	public static void main(String[] args) {
		Wordle wordle = new Wordle();
		wordle.run();
	}

}
