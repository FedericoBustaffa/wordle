import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class Server {

	// CONFIGURATION
	private static final int RMI_PORT = 1500;
	private static final int TCP_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration;

	// TCP
	private SocketAddress tcp_service;
	private Selector selector;

	public Server(Set<User> users) {
		try {
			// RMI
			registration = new RegistrationService(users);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration);

			// TCP
			tcp_service = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);
			selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.bind(tcp_service);
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void accept() {

	}

	public void send(String response) {

	}

	public String receive() {
		return null;
	}

	public void multiplex() {

	}

	public void shutdown() {
		try {
			// RMI closure
			registry.unbind(Registration.SERVICE);
			UnicastRemoteObject.unexportObject(registration, false);

			// TCP closure
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}
