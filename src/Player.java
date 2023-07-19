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

	private void register(String[] cmd) {
		client.register(cmd);
	}

	private void login(String[] cmd) {
		client.send(cmd);
		String response = client.receive();
		// controlli
	}

	private void logout(String[] cmd) {
		client.send(cmd);
		String response = client.receive();
	}

	private void exit(String[] cmd) {
		client.send(cmd);
		String response = client.receive();
	}

	public void shell() {
		String[] cmd;
		while (!done) {
			System.out.printf("> ");
			cmd = input.nextLine().trim().split(" ");
			switch (cmd[0]) {
				case "register":
					register(cmd);
					break;
				case "login":
					login(cmd);
					break;
				case "logout":
					logout(cmd);
					break;
				case "exit":
					exit(cmd);
					break;
			}
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
