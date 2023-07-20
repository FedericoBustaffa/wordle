import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
	private SocketAddress tcp_address;
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	// private ObjectInputStream object_reader;
	// private ObjectOutputStream object_writer;

	public Client() {
		try {
			// RMI
			registry = LocateRegistry.getRegistry(RMI_PORT);
			registration = (Registration) registry.lookup(Registration.SERVICE);

			// TCP
			tcp_address = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);
			socket = new Socket();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			socket.connect(tcp_address);
			reader = new DataInputStream(socket.getInputStream());
			writer = new DataOutputStream(socket.getOutputStream());
			// object_writer = new ObjectOutputStream(socket.getOutputStream());
			// object_reader = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String register(String cmd) {
		try {
			String[] parse = cmd.split(" ");
			if (parse.length != 3)
				return "< ERROR\n< USAGE: register <username> <password>";

			return registration.register(parse[1], parse[2]);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void send(String cmd) {
		try {
			writer.writeUTF(cmd);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUser(User user) {

	}

	public String receive() {
		try {
			return reader.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public User receiveUser() {
		return null;
	}

	public void shutdown() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}