import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Receiver implements Runnable {

	private Selector selector;
	private SocketChannel socket;
	private ByteStream stream;

	private Set<User> users;

	public Receiver(Selector selector, SocketChannel socket, ByteStream stream, Set<User> users) {
		this.selector = selector;
		this.socket = socket;
		this.stream = stream;
		this.users = users;
	}

	private void login(String[] cmd) {
		if (cmd.length != 3) {
			stream.write("ERROR USAGE: login <username> <password>");
			return;
		}

		String username = cmd[1];
		String password = cmd[2];
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isOnline()) {
						u.online();
						stream.write("login success");
						System.out.println(username + " logged in");
					} else {
						stream.write("ERROR: already logged in");
					}
				} else {
					stream.write("ERROR: wrong password");
				}
				return;
			}
		}
		stream.write("ERROR: user " + username + " not registered");
	}

	private void play(String[] cmd) {
		if (cmd.length != 2) {
			stream.write("ERROR USAGE: play");
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			stream.write("ERROR: login to play");
			return;
		} else {
			int score = new Random().nextInt(10);
			Iterator<User> it = users.iterator();
			User user;
			while (it.hasNext()) {
				user = it.next();
				if (username.equals(user.getUsername())) {
					it.remove();
					user.setScore(score);
					users.add(user);
					stream.write("play: " + username + " " + score);
					return;
				}
			}
		}
	}

	private void share(String[] cmd) {
		if (cmd.length != 3) {
			stream.write("ERROR USAGE: share");
			return;
		}

		String username = cmd[1];
		int last_score = Integer.parseInt(cmd[2]);
		if (username.equals("null")) {
			stream.write("ERROR: login to share your score");
			return;
		} else if (last_score == -1) {
			stream.write("ERROR: play at least one game to share your score");
			return;
		}

		stream.write("share: " + username + " " + last_score);
	}

	private void logout(String[] cmd) {
		if (cmd.length != 2) {
			stream.write("ERROR USAGE: logout");
			return;
		}

		String username = cmd[1];
		if (username.equals("null")) {
			stream.write("ERROR: login before logout");
			return;
		}

		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (u.isOnline()) {
					u.offline();
					stream.write("logout success: " + username);
					System.out.println(username + " has left");
				} else {
					stream.write("ERROR: not logged in yet");
				}
				return;
			}
		}
		stream.write("ERROR: user " + username + " not present");
	}

	private void exit(String[] cmd) {
		if (cmd.length != 2) {
			stream.write("ERROR USAGE: exit");
			return;
		} else if (!cmd[1].equals("null")) {
			String username = cmd[1];
			for (User u : users) {
				if (username.equals(u.getUsername())) {
					u.offline();
					stream.write("exit success " + username);
					System.out.println(username + " logged out");
					return;
				}
			}
			stream.write("ERROR: username " + username + " not present");
		} else {
			stream.write("exit success");
		}
	}

	public void run() {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(512);
			buffer.clear();
			int b = socket.read(buffer);
			if (b != -1) {
				String[] cmd = new String(buffer.array(), 0, b).split(" ");
				String first = cmd[0].trim();
				if (first.equals("login"))
					login(cmd);
				else if (first.equals("play"))
					play(cmd);
				else if (first.equals("share"))
					share(cmd);
				else if (first.equals("logout"))
					logout(cmd);
				else if (first.equals("exit"))
					exit(cmd);

				socket.register(selector, SelectionKey.OP_WRITE, stream);
				selector.wakeup();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
