import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

	// current user account
	private User user;

	// configuration
	private static int RMI_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration_service;

	// TCP
	private Selector selector;
	private SocketChannel socket;

	// keyboard input
	private Scanner input;

	public Client() {
		try {
			// user profile
			user = null;

			// RMI service for registration
			registry = LocateRegistry.getRegistry(RMI_PORT);
			registration_service = (Registration) registry.lookup(Registration.SERVICE);

			// TCP
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			selector = Selector.open();
			socket.register(selector, SelectionKey.OP_CONNECT);

			// keyboard input
			input = new Scanner(System.in);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registration() {
		try {
			System.out.printf("< username: ");
			String username = input.nextLine().trim();
			System.out.printf("< password: ");
			String password = input.nextLine().trim();
			System.out.println("< " + registration_service.register(username, password));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void login() {
		System.out.printf("< username: ");
		String username = input.nextLine().trim();
		System.out.printf("< password: ");
		String password = input.nextLine().trim();
	}

	private void logout() {

	}

	public void shell() {
		String cmd;
		do {
			System.out.printf("> ");
			cmd = input.nextLine().trim();
			switch (cmd) {
				case "register":
					registration();
					break;
				case "login":
					login();
					break;
				case "logout":
					logout();
					break;
				case "quit":
					break;
				default:
					System.out.println("< invalid command");
					break;
			}
		} while (!cmd.equals("quit"));
	}

	public void shutdown() {
		input.close();
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.shell();
		client.shutdown();
	}
}
