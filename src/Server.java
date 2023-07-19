import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.Set;

public class Server {

	// social network and game data
	private Wordle wordle;

	// configuration values
	private static final String BACKUP_USERS = "users.json";
	private static final int RMI_PORT = 2000;

	// Json backup
	private JsonUser json_user;

	// RMI
	private Registry registry;
	private Registration registration_service;

	public Server() {
		try {
			// wordle init
			wordle = new Wordle();

			// Json
			json_user = new JsonUser(BACKUP_USERS);
			Set<User> users = json_user.readArray();
			if (users == null) {
				System.out.println("< backup file error");
				System.exit(1);
			}
			for (User u : users) {
				wordle.add(u);
			}

			registration_service = new RegistrationService(wordle);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration_service);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}

	public void shutdown() {
		try {
			// json backup file creation
			json_user.writeArray(wordle.getUsers());

			// RMI service closure
			UnicastRemoteObject.unexportObject(registration_service, false);
			registry.unbind(Registration.SERVICE);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		server.shutdown();
	}
}
