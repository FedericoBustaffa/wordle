import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

	// Tree Set of Users
	private Set<User> users;
	// private Set<User> playing_users; // giocatori online

	// JSON
	private JsonWrapper json_wrapper;

	// CONFIGURATION
	private String BACKUP_USERS;
	private int RMI_PORT;
	private int TCP_PORT;
	private String MULTICAST_ADDRESS;
	private int MULTICAST_PORT;

	// RMI
	private Registry registry;
	private Registration registration;

	// TCP
	private AtomicInteger ACTIVE_CONNECTIONS;
	private SocketAddress tcp_service;
	private Selector selector;
	private ExecutorService pool;

	// MULTICAST
	private MulticastSocket multicast;
	private SocketAddress group;

	public Server() {
		try {
			// configuration file
			File config = new File("server_config.txt");
			if (!config.exists()) {
				System.out.println("ERROR: server configuration file not found");
				System.exit(1);
			}

			Scanner scanner = new Scanner(config);
			String[] line;
			while (scanner.hasNext()) {
				line = scanner.nextLine().split("=");
				if (line[0].equals("BACKUP_USERS"))
					BACKUP_USERS = line[1];
				else if (line[0].equals("RMI_PORT"))
					RMI_PORT = Integer.parseInt(line[1]);
				else if (line[0].equals("TCP_PORT"))
					TCP_PORT = Integer.parseInt(line[1]);
				else if (line[0].equals("MULTICAST_ADDRESS"))
					MULTICAST_ADDRESS = line[1];
				else if (line[0].equals("MULTICAST_PORT"))
					MULTICAST_PORT = Integer.parseInt(line[1]);
				else {
					System.out.println("< ERROR: configuration file corrupted");
					System.exit(1);
				}
			}
			scanner.close();

			// playing users init
			// playing_users = Collections.synchronizedSet(new TreeSet<User>());

			// Json backup
			json_wrapper = new JsonWrapper(BACKUP_USERS);
			users = Collections.synchronizedSet(json_wrapper.readArray());

			// RMI
			registration = new RegistrationService(users);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration);
			System.out.println("< RMI service on port: " + RMI_PORT);

			// TCP
			ACTIVE_CONNECTIONS = new AtomicInteger(0);
			tcp_service = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);

			selector = Selector.open();

			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(tcp_service);
			server.register(selector, SelectionKey.OP_ACCEPT);

			pool = Executors.newCachedThreadPool();
			System.out.println("< TCP connections available on port: " + TCP_PORT);

			// MULTICAST
			multicast = new MulticastSocket();
			group = new InetSocketAddress(MULTICAST_ADDRESS, MULTICAST_PORT);
			// multicast.joinGroup(group, null);
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel socket = server.accept();
			socket.configureBlocking(false);
			socket.register(selector, SelectionKey.OP_READ, new ByteStream(1024));
			ACTIVE_CONNECTIONS.incrementAndGet();
			System.out.println("< new client connected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void multiplex() {
		try {
			selector.select();
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			SelectionKey key;
			while (it.hasNext()) {
				key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					this.accept(key);
				} else {
					SocketChannel socket = (SocketChannel) key.channel();
					ByteStream stream = (ByteStream) key.attachment();
					if (key.isWritable()) {
						key.cancel();
						pool.execute(new Writer(selector, socket, stream, ACTIVE_CONNECTIONS));
					} else if (key.isReadable()) {
						key.cancel();
						pool.execute(new Reader(selector, socket, stream, multicast, group, users));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getActiveConnections() {
		return ACTIVE_CONNECTIONS.get();
	}

	public void shutdown() {
		try {
			pool.shutdown();
			while (!pool.awaitTermination(60L, TimeUnit.SECONDS))
				;

			// JSON backup
			json_wrapper.writeArray(users);

			// RMI closure
			registry.unbind(Registration.SERVICE);
			UnicastRemoteObject.unexportObject(registration, false);

			// TCP closure
			for (SelectionKey k : selector.keys()) {
				k.channel().close();
			}
			selector.close();

			// multicast closure
			multicast.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		do {
			server.multiplex();
		} while (server.getActiveConnections() > 0);
		server.shutdown();
	}
}
