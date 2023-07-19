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

	// keyboard input
	private Scanner input;

	public Client() {
		try {
			user = null;
			registry = LocateRegistry.getRegistry(RMI_PORT);
			registration_service = (Registration) registry.lookup(Registration.SERVICE);
			input = new Scanner(System.in);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void shell() {
		String cmd;
		do {
			System.out.printf("> ");
			cmd = input.nextLine();
			switch (cmd) {
				case "register":
					registration();
					break;
				case "quit":
					break;
				default:
					System.out.println("< invalid command");
					break;
			}
		} while (!cmd.equals("quit"));
	}

	private void registration() {
		try {
			System.out.printf("< username: ");
			String username = input.nextLine();
			System.out.printf("< password: ");
			String password = input.nextLine();
			registration_service.register(username, password);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
