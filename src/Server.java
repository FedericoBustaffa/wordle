import java.io.File;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server {

	// social network and game data
	private Wordle wordle;

	// configuration
	private static final int RMI_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration_service;

	// Json backup
	private File backup;

	public Server() {
		try {
			wordle = new Wordle();

			registration_service = new RegistrationService(wordle);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration_service);

			backup = new File("backup.json");
			if (!backup.exists())
				backup.createNewFile();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
