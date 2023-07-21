import java.util.Scanner;

public class Player {

	private String username;
	private Client client;
	private boolean done;
	private Scanner input;

	public Player() {
		done = false;
		username = null;
		client = new Client();
		input = new Scanner(System.in);
	}

	private void login(String cmd) {
		if (username != null) {
			System.out.println("< logout before login");
			return;
		}

		client.send(cmd);
		String response = client.receive();
		System.out.println(response);
		if (!response.contains("ERROR")) {
			username = response.split(" ")[3];
		}
	}

	private void logout(String cmd) {
		client.send(cmd + " " + username);
		String response = client.receive();
		System.out.println(response);
		if (!response.contains("ERROR"))
			username = null;
	}

	private void play(String cmd) {
		client.send(cmd + " " + username);
		String response = client.receive();
		System.out.println(response);
	}

	private void exit(String cmd) {
		client.send(cmd + " " + username);
		String response = client.receive();
		System.out.println(response);
		if (!response.contains("ERROR")) {
			username = null;
			done = true;
		}
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
				this.login(cmd);
			else if (first.equals("logout"))
				this.logout(cmd);
			else if (first.equals("play"))
				this.play(cmd);
			else if (first.equals("exit"))
				this.exit(cmd);
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
