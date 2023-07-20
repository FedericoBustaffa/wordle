import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;

public class Server {

	// Tree Set of Users
	private Set<User> users;

	// JSON
	private JsonWrapper json_wrapper;

	// CONFIGURATION
	private static final String BACKUP_USERS = "users.json";
	private static final int RMI_PORT = 1500;
	private static final int TCP_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration;

	// TCP
	public static int ACTIVE_CONNECTIONS = 0;
	private SocketAddress tcp_service;
	private Selector selector;

	public Server() {
		try {
			// Json backup
			json_wrapper = new JsonWrapper(BACKUP_USERS);
			users = json_wrapper.readArray();

			// RMI
			registration = new RegistrationService(users);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration);
			System.out.println("< RMI service on");

			// TCP
			tcp_service = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);
			selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(tcp_service);
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("< TCP connections available");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel socket = server.accept();
			socket.configureBlocking(false);
			socket.register(selector, SelectionKey.OP_READ, new ByteStream(1024));
			ACTIVE_CONNECTIONS++;
			System.out.println("< new client connected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void login(String[] cmd, ByteStream stream) {
		if (cmd.length != 3) {
			stream.write("< ERROR USAGE: login <username> <password>");
			return;
		}

		String username = cmd[1];
		String password = cmd[2];
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isOnline()) {
						u.online();
						stream.writeUser(u);
					} else {
						stream.write("< ERROR: already logged in");
					}
				} else {
					stream.write("< ERROR: wrong password");
				}
				return;
			}
		}
		stream.write("< ERROR: user not registered");
		System.out.println("< login success");
	}

	private void logout(String[] cmd, ByteBuffer buffer) {
		buffer.clear();
		if (cmd.length != 2) {
			buffer.put("< ERROR USAGE: logout".getBytes());
			return;
		}

		String username = cmd[1];
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (u.isOnline()) {
					u.offline();
					buffer.put("< logout success".getBytes());
				} else {
					buffer.put("< ERROR: not logged in yet".getBytes());
				}
				return;
			}
		}
		buffer.put(("< ERROR: username " + username + " not present").getBytes());
	}

	private void exit(String[] cmd, ByteBuffer buffer) {
		buffer.clear();
		if (cmd.length == 1) {
			buffer.put("< exit success".getBytes());
		} else if (cmd.length == 2) {
			String username = cmd[1];
			for (User u : users) {
				if (username.equals(u.getUsername())) {
					if (u.isOnline()) {
						u.offline();
						buffer.put("< exit success".getBytes());
					} else {
						buffer.put("< ERROR: not logged in yet".getBytes());
					}
					return;
				}
			}
			buffer.put(("< ERROR: username " + username + " not present").getBytes());
		} else {
			buffer.put("< ERROR USAGE: exit".getBytes());
		}
	}

	private void receive(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteStream stream = (ByteStream) key.attachment();
			String[] cmd = stream.read().split(" ");
			String first = cmd[0].trim();
			System.out.println("< " + first);
			if (first.equals("login"))
				login(cmd, stream);
			/*
			 * else if (first.equals("logout"))
			 * logout(cmd, buffer);
			 * else if (first.equals("exit")) {
			 * exit(cmd, buffer);
			 * ACTIVE_CONNECTIONS--;
			 * key.cancel();
			 * socket.close();
			 * return;
			 * }
			 */else
				System.out.println("< invalid command");
			socket.register(selector, SelectionKey.OP_WRITE, stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteStream stream = (ByteStream) key.attachment();
			ByteBuffer buffer = ByteBuffer.wrap(stream.getBytes());
			buffer.flip();
			while (buffer.hasRemaining())
				socket.write(buffer);
			socket.register(selector, SelectionKey.OP_READ, stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void multiplex() {
		try {
			selector.select();
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			SelectionKey k;
			while (it.hasNext()) {
				k = it.next();
				it.remove();
				if (k.isAcceptable()) {
					System.out.println("< ACCEPT");
					accept(k);
				} else if (k.isReadable()) {
					System.out.println("< READ");
					receive(k);
				} else if (k.isWritable()) {
					System.out.println("< WRITE");
					send(k);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			// JSON backup
			json_wrapper.writeArray(users);

			// RMI closure
			registry.unbind(Registration.SERVICE);
			UnicastRemoteObject.unexportObject(registration, false);

			// TCP closure
			for (SelectionKey k : selector.keys())
				k.channel().close();
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		do {
			server.multiplex();
		} while (ACTIVE_CONNECTIONS > 0);
		server.shutdown();
	}
}
