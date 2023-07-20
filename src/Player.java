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
		client.send(cmd);
		String response = client.receive();
		if (response.contains("error")) {
			System.out.println("< " + response);
		} else {
			System.out.println("< login success");
			String[] parse = response.split(" ");
			// user = new User(parse[0], parse[1]);
		}
	}

	private void logout(String cmd) {
		client.send(cmd);
		String response = client.receive();
		if (response.contains("ERROR")) {
			System.out.println("< " + response);
		} else {
			System.out.println("< logout success");
			user = null;
		}
	}

	private void exit(String cmd) {
		client.send(cmd);
		String response = client.receive();
		if (response.contains("ERROR")) {
			System.out.println("< " + response);
		} else {
			System.out.println("< exit");
			user = null;
		}
	}

	public void shell() {
		client.connect();
		String cmd;
		while (!done) {
			System.out.printf("> ");
			cmd = input.nextLine().trim();
			switch (cmd.split(" ")[0]) {
				case "register":
					System.out.println(client.register(cmd));
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
				default:
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
