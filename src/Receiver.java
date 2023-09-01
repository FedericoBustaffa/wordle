import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver implements Runnable {

	private SelectionKey key;
	private Attachment attachment;
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer buffer;

	private ConcurrentHashMap<String, User> users;
	private List<User> ranking;
	private Wordle wordle;

	public Receiver(SelectionKey key) {
		this.key = key;
		this.attachment = (Attachment) key.attachment();
		this.selector = key.selector();
		this.socket = (SocketChannel) key.channel();
		this.buffer = attachment.getBuffer();
		this.users = attachment.getUsers();
		this.ranking = attachment.getRanking();
		this.wordle = attachment.getWordle();
	}

	private void help(String[] cmd) {
		if (cmd.length != 1) {
			buffer.put("ERROR USAGE: help".getBytes());
			return;
		}

		String commands = "--------------- HELP ---------------\n" +
				"< register <username> <password>\n" +
				"< login <username> <password>\n" +
				"< play\n" +
				"< guess <word>\n" +
				"< statistics\n" +
				"< share\n" +
				"< show\n" +
				"< ranking\n" +
				"< logout\n" +
				"< exit\n" +
				"< ------------------------------------";

		buffer.put(commands.getBytes());
	}

	private void login(String[] cmd) {
		if (cmd.length != 3) {
			buffer.put("ERROR USAGE: login <username> <password>".getBytes());
			return;
		}
		if (users == null) {
			buffer.put("ERROR: register to login".getBytes());
			return;
		}

		String username = cmd[1];
		String password = cmd[2];
		User u = users.get(username);
		if (u == null) {
			buffer.put("ERROR: register to login".getBytes());
			return;
		}

		if (password.equals(u.getPassword())) {
			if (u.isOnline()) {
				buffer.put("ERROR: user already logged in".getBytes());
			} else {
				u.online();
				buffer.put(("SUCCESS: login " + username).getBytes());
				System.out.println("< " + username + " logged in");
			}
		} else {
			buffer.put("ERROR: wrong password".getBytes());
		}
	}

	private void play(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: play".getBytes());
			return;
		}

		if (users == null) {
			buffer.put("ERROR: register to play".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: login to play".getBytes());
			return;
		}

		User u = users.get(username);
		if (wordle.startSession(username)) {
			// wordle.getSessions();
			u.incGames();
			buffer.put("SUCCESS: game started".getBytes());
		} else
			buffer.put("ERROR: you can't start a new game now".getBytes());
	}

	private void guess(String[] cmd) {
		if (cmd.length != 3) {
			buffer.put("ERROR USAGE: guess <word>".getBytes());
			return;
		}

		String username = cmd[2];
		if (username.equals("null")) {
			buffer.put("ERROR: login to guess the word of the day".getBytes());
			return;
		}

		String word = cmd[1];
		String guess_result = wordle.guess(username, word);
		Session s = wordle.get(username);
		if (s != null) {
			int attempts = s.getAttempts();
			User u;
			if (guess_result.contains("right")) {
				u = users.get(username);
				u.incWins();
				u.updateGuessDistribution(attempts);
				wordle.closeSession(username);
			} else if (attempts >= 12) {
				u = users.get(username);
				u.resetLastStreak();
			}
		}
		buffer.put((guess_result).getBytes());
	}

	private void statistics(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: statistics".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: login to see your statistics".getBytes());
			return;
		}

		User user = users.get(username);
		buffer.put(user.statistics().getBytes());
	}

	private void share(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: share".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: login to share your score".getBytes());
			return;
		}

		Session s = wordle.get(username);
		if (s == null) {
			buffer.put("ERROR: you have to play at least one game".getBytes());
		} else {
			if (!s.isClose())
				buffer.put("ERROR: you have to finish the current game".getBytes());
			else
				buffer.put(("SUCCESS: share " + username + ", " + s).getBytes());
		}
	}

	private void ranking(String[] cmd) {
		if (cmd.length != 1) {
			buffer.put("ERROR USAGE: ranking".getBytes());
			return;
		}

		if (ranking.isEmpty()) {
			buffer.put("empty ranking list".getBytes());
			return;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("----------- RANKING LIST -----------\n");
		for (User u : ranking)
			builder.append("< " + u + "\n");
		builder.append("< ------------------------------------");

		buffer.put(builder.toString().getBytes());
	}

	private void logout(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: logout".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: you're not logged in yet".getBytes());
			return;
		}

		User u = users.get(username);
		if (u == null) {
			buffer.put(("ERROR: user " + username + " not present").getBytes());
			return;
		}

		if (!u.isOnline()) {
			buffer.put("ERROR: not logged yet".getBytes());
		} else {
			u.offline();
			System.out.println("< " + username + " left");
			Session session = wordle.get(username);
			if (session != null && !session.isClose()) {
				session.lose();
				session.close();
				u.resetLastStreak();
				buffer.put(("SUCCESS: logout " + username + " " + session.getWord()).getBytes());
			} else
				buffer.put(("SUCCESS: logout " + username).getBytes());
		}
	}

	private void exit(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: exit".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("SUCCESS: exit".getBytes());
			return;
		}

		User u = users.get(username);
		if (u == null) {
			buffer.put(("ERROR: username " + username + " not present").getBytes());
			return;
		} else {
			u.offline();
			System.out.println("< " + username + " left");
			Session session = wordle.get(username);
			if (session != null && !session.isClose()) {
				session.lose();
				session.close();
				u.resetLastStreak();
				buffer.put(("SUCCESS: exit " + username + " " + session.getWord()).getBytes());
			} else
				buffer.put(("SUCCESS: exit " + username).getBytes());
		}
	}

	public void run() {
		try {
			buffer.clear();
			int b = socket.read(buffer);
			if (b != -1) {
				String[] cmd = new String(buffer.array(), 0, b).split(" ");
				String first = cmd[0].trim();
				buffer.clear();
				if (first.equals("help"))
					help(cmd);
				else if (first.equals("login"))
					login(cmd);
				else if (first.equals("play"))
					play(cmd);
				else if (first.equals("guess"))
					guess(cmd);
				else if (first.equals("statistics"))
					statistics(cmd);
				else if (first.equals("share"))
					share(cmd);
				else if (first.equals("ranking"))
					ranking(cmd);
				else if (first.equals("logout"))
					logout(cmd);
				else if (first.equals("exit"))
					exit(cmd);

				key.interestOps(SelectionKey.OP_WRITE);
				key.attach(attachment);
				selector.wakeup();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
