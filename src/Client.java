import java.io.IOException;
import java.nio.channels.Selector;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	// CONFIGURATION
	private static final int RMI_PORT = 1500;
	private static final int TCP_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration;

	// TCP
	private Selector selector;

	public Client() {
		try {
			// RMI
			registry = LocateRegistry.getRegistry(RMI_PORT);
			registration = (Registration) registry.lookup(Registration.SERVICE);

			// TCP
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void register(String[] cmd) {
		try {
			if (cmd.length != 3)
				System.out.println("< USAGE: register <username> <password>");

			System.out.println("< " + registration.register(cmd[1], cmd[2]));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void send(String[] cmd) {

	}

	public String receive() {
		return null;
	}

	public void shutdown() {
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}