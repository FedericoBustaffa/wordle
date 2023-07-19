import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;

public class Server {

	// social network and game data
	private Wordle wordle;

	// configuration values
	private static final String BACKUP_USERS = "users.json";
	private static final int RMI_PORT = 2000;
	private static final int TCP_PORT = 3000;

	// Json backup
	private JsonUser json_user;

	// RMI
	private Registry registry;
	private Registration registration_service;

	// Threadpool

	// TCP
	private int ACTIVE_CONNECTIONS;
	private Selector selector;

	public Server() {
		try {
			// wordle init
			wordle = new Wordle();

			// Json backup recovery
			json_user = new JsonUser(BACKUP_USERS);
			Set<User> users = json_user.readArray();

			if (users == null) {
				System.out.println("< backup file error");
				System.exit(1);
			}

			for (User u : users) {
				wordle.add(u);
			}

			// RMI service dispatch
			registration_service = new RegistrationService(wordle);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration_service);

			// TCP
			ACTIVE_CONNECTIONS = 0;
			selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.bind(new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT));
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) {
		try {
			System.out.println("< waiting for connections...");
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel socket = server.accept();
			ACTIVE_CONNECTIONS++;
			System.out.println("ACTIVE CONNECTIONS: " + ACTIVE_CONNECTIONS);
			socket.configureBlocking(false);
			System.out.println("< accepted connection from: " + socket.getRemoteAddress());
			socket.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(32));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receive(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteBuffer buffer = (ByteBuffer) key.attachment();
			buffer.clear();
			int b = socket.read(buffer);
			String msg = new String(buffer.array(), 0, b);
			System.out.println(msg);
			if (msg.equals("exit")) {
				ACTIVE_CONNECTIONS--;
				System.out.println("ACTIVE CONNECTIONS: " + ACTIVE_CONNECTIONS);
				key.cancel();
				return;
			}
			key.interestOps(SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteBuffer buffer = (ByteBuffer) key.attachment();
			buffer.flip();
			while (buffer.hasRemaining())
				socket.write(buffer);

			key.interestOps(SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			while (ACTIVE_CONNECTIONS > 0) {
				selector.select();
				Set<SelectionKey> readyKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = readyKeys.iterator();
				SelectionKey k;
				while (it.hasNext()) {
					k = it.next();
					it.remove();
					if (k.isAcceptable()) {
						System.out.println("ACCEPT");
						accept(k);
					} else if (k.isReadable()) {
						System.out.printf("READ: ");
						receive(k);
					} else if (k.isWritable()) {
						System.out.println("WRITE");
						send(k);
					}
					System.out.println("- - - - - - - -");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			// json backup file creation
			json_user.writeArray(wordle.getUsers());

			// RMI service closure
			UnicastRemoteObject.unexportObject(registration_service, false);
			registry.unbind(Registration.SERVICE);

			// TCP closure
			selector.close();

		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		server.shutdown();
	}
}
