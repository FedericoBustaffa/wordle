import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Receiver implements Runnable {

	private SelectionKey key;
	private Attachment attachment;
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer buffer;

	private Set<User> users;
	private Wordle wordle;

	public Receiver(SelectionKey key) {
		this.key = key;
		this.attachment = (Attachment) key.attachment();
		this.selector = key.selector();
		this.socket = (SocketChannel) key.channel();
		this.buffer = attachment.getBuffer();
		this.users = attachment.getUsers();
		this.wordle = attachment.getWordle();
	}

	private void help(String[] cmd) {
		if (cmd.length != 1) {
			buffer.put("ERROR USAGE: help".getBytes());
			return;
		}

		String commands = "--- HELP ---\n" +
				"< register <username> <password>\n" +
				"< login <username> <password>\n" +
				"< play\n" +
				"< guess <word>\n" +
				"< share\n" +
				"< show\n" +
				"< logout\n" +
				"< exit";

		buffer.put(commands.getBytes());
	}

	private void login(String[] cmd) {
		if (cmd.length != 3) {
			buffer.put("ERROR USAGE: login <username> <password>".getBytes());
			return;
		}

		String username = cmd[1];
		String password = cmd[2];
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isOnline()) {
						u.online();
						buffer.put("login success".getBytes());
						System.out.println("< " + username + " logged in");
					} else {
						buffer.put("ERROR: already logged in".getBytes());
					}
				} else {
					buffer.put("ERROR: wrong password".getBytes());
				}
				return;
			}
		}
		buffer.put(("ERROR: user " + username + " not registered").getBytes());
	}

	private void play(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: play".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: login to play".getBytes());
			return;
		} else {
			synchronized (users) {
				Iterator<User> it = users.iterator();
				User user;
				while (it.hasNext()) {
					user = it.next();
					if (username.equals(user.getUsername())) {
						if (wordle.startSession(username)) {
							System.out.println("< " + wordle.getSessions());
							buffer.put("game started".getBytes());
						} else
							buffer.put("you can't start a new game now".getBytes());
						return;
					}
				}
			}
		}
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
		if (guess_result.contains("right")) {
			wordle.endSession(username);
			System.out.println("< " + wordle.getSessions());
		}
		buffer.put(guess_result.getBytes());
	}

	private void share(String[] cmd) {
		if (cmd.length != 3) {
			buffer.put("ERROR USAGE: share".getBytes());
			return;
		}

		String username = cmd[1];
		int last_score = Integer.parseInt(cmd[2]);
		if (username.equals("null")) {
			buffer.put("ERROR: login to share your score".getBytes());
			return;
		} else if (last_score == -1) {
			buffer.put("ERROR: play at least one game to share your score".getBytes());
			return;
		}

		buffer.put(("share: " + username + " " + last_score).getBytes());
	}

	private void logout(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: logout".getBytes());
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			buffer.put("ERROR: login before logout".getBytes());
			return;
		}

		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (u.isOnline()) {
					u.offline();
					wordle.endSession(username);
					System.out.println("< " + wordle.getSessions());
					buffer.put(("logout success: " + username).getBytes());
					System.out.println("< " + username + " left");
				} else {
					buffer.put(("ERROR: not logged in yet").getBytes());
				}
				return;
			}
		}
		buffer.put(("ERROR: user " + username + " not present").getBytes());
	}

	private void exit(String[] cmd) {
		if (cmd.length != 2) {
			buffer.put("ERROR USAGE: exit".getBytes());
			return;
		} else if (!cmd[1].equals("null")) {
			String username = cmd[1];
			for (User u : users) {
				if (username.equals(u.getUsername())) {
					u.offline();
					wordle.endSession(username);
					System.out.println("< " + wordle.getSessions());
					buffer.put(("exit success " + username).getBytes());
					System.out.println("< " + username + " left");
					return;
				}
			}
			buffer.put(("ERROR: username " + username + " not present").getBytes());
		} else {
			buffer.put("exit success".getBytes());
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
				else if (first.equals("share"))
					share(cmd);
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
