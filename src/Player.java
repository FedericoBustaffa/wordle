import java.util.Scanner;

public class Player {

	private User user;
	private Client client;
	private boolean done;
	private Scanner input;

	public Player() {
		done = false;
		user = null;
		client = new Client();
		input = new Scanner(System.in);
	}

	private void login(String cmd) {
		if (user != null) {
			System.out.println("< logout before login");
			return;
		}

		client.send(cmd);
		String response = client.receive();
		System.out.println(response);
		if (!response.contains("ERROR")) {
			// user = client.receiveUser();
		}
	}

	private void logout(String cmd) {
		if (user == null) {
			System.out.println("< login before logout");
			return;
		}
		client.send(cmd + " " + user.getUsername());
		String response = client.receive();
		System.out.println(response);
		if (!response.contains("ERROR"))
			user = null;
	}

	private void exit(String cmd) {
		if (user != null)
			cmd = cmd + " " + user.getUsername();
		client.send(cmd);
		user = null;
		done = true;
	}

	public void shell() {
		client.connect();
		String cmd;
		String first;
		while (!done) {
			System.out.printf("> ");
			cmd = input.nextLine().trim();
			first = cmd.split(" ")[0];

			if (first.equals("register"))
				System.out.println(client.register(cmd));
			else if (first.equals("login"))
				login(cmd);
			else if (first.equals("logout"))
				logout(cmd);
			else if (first.equals("exit"))
				exit(cmd);
			else
				System.out.println("< invalid command");
		}
	}

	public void shutdown() {
		client.shutdown();
		input.close();
	}

	public static void main(String[] args) {
		Player player = new Player();
		player.shell();
		player.shutdown();
	}
}
