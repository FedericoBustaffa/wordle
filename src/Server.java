import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {

	// tabella hash degli utenti e classifica punteggi
	private ConcurrentHashMap<String, User> users;
	private List<User> ranking;

	// Wordle e thread estrattore
	private Wordle wordle;
	private Thread extractor;

	// JSON
	private JsonWrapper json_wrapper;

	// variabili di configurazione
	private String USERS_BACKUP;
	private String WORDS;
	private int RMI_PORT;
	private int TCP_PORT;
	private String MULTICAST_ADDRESS;
	private int MULTICAST_PORT;
	private long EXTRACTION_TIMEOUT;

	// condizioni per l'esecuzione
	private boolean RUNNING;
	private volatile AtomicInteger ACTIVE_CONNECTIONS;

	// RMI
	private Registry registry;
	private Registration registration;
	private List<Notify> notifiers;

	// TCP
	private SocketAddress tcp_service;
	private Selector selector;
	private SelectionKey server_socket_key;
	private ExecutorService pool;

	// MULTICAST
	private MulticastSocket multicast;
	private SocketAddress group;

	public Server(String config_file) {
		try {
			System.out.println("< -------------- WORDLE --------------");

			// parsing del file di configurazione
			File config = new File(config_file);
			if (!config.exists()) {
				System.out.println("< ERROR: server configuration file not found");
				System.exit(1);
			}

			json_wrapper = new JsonWrapper(config);
			String conf = json_wrapper.getContent();

			USERS_BACKUP = json_wrapper.getString(conf, "users_backup_file");
			WORDS = json_wrapper.getString(conf, "words_file");
			RMI_PORT = json_wrapper.getInteger(conf, "rmi_port");
			TCP_PORT = json_wrapper.getInteger(conf, "tcp_port");
			MULTICAST_ADDRESS = json_wrapper.getString(conf, "multicast_address");
			MULTICAST_PORT = json_wrapper.getInteger(conf, "multicast_port");
			EXTRACTION_TIMEOUT = json_wrapper.getLong(conf, "extraction_timeout");

			// Json wrapper for backup
			File backup = new File(USERS_BACKUP);
			if (!backup.exists()) {
				backup.createNewFile();
			}
			json_wrapper = new JsonWrapper(backup);
			users = json_wrapper.readArray();

			// build the ranking list
			ranking = Collections.synchronizedList(new LinkedList<User>());
			if (users != null) {
				for (User u : users.values())
					ranking.add(u);
			}
			Collections.sort(ranking);

			// Wordle init
			wordle = new Wordle(new File(WORDS), EXTRACTION_TIMEOUT);
			extractor = new Thread(wordle);

			// RMI
			notifiers = Collections.synchronizedList(new LinkedList<Notify>());
			registration = new RegistrationService(users, ranking, notifiers);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration);
			System.out.println("< RMI service on port: " + RMI_PORT);

			// TCP
			RUNNING = true;

			ACTIVE_CONNECTIONS = new AtomicInteger(0);
			tcp_service = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);

			selector = Selector.open();

			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(tcp_service);
			server_socket_key = server.register(selector, SelectionKey.OP_ACCEPT);

			pool = Executors.newCachedThreadPool();
			System.out.println("< TCP connections available on port: " + TCP_PORT);

			// MULTICAST
			multicast = new MulticastSocket();
			group = new InetSocketAddress(MULTICAST_ADDRESS, MULTICAST_PORT);
			System.out.println("< MULTICAST address: " + MULTICAST_ADDRESS);
			System.out.println("< MULTICAST port: " + MULTICAST_PORT);

			System.out.println("< premere INVIO per terminare l'esecuzione");
			System.out.println("< ------------------------------------");

			// extractor thread start
			extractor.start();
		} catch (NoSuchElementException e) {
			System.out.println("< invalid server configuration file: " +
					e.getLocalizedMessage() + " field not found");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// thread in attesa di input per spegnere il server
	@Override
	public void run() {
		try {
			Scanner shutdown_button = new Scanner(System.in);
			shutdown_button.nextLine();
			shutdown_button.close();

			// chiusura server TCP
			server_socket_key.channel().close();
			RUNNING = false;
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// accetta nuove connessioni e incrementa il numero di client connessi
	private void accept(SelectionKey key) {
		try {
			synchronized (ACTIVE_CONNECTIONS) {
				ServerSocketChannel server = (ServerSocketChannel) key.channel();
				SocketChannel socket = server.accept();
				socket.configureBlocking(false);
				System.out.println("< new client connected: " +
						ACTIVE_CONNECTIONS.incrementAndGet() + " clients connected");
				ByteBuffer buffer = ByteBuffer.allocate(512);
				Attachment attachment = new Attachment(buffer, users, ranking, ACTIVE_CONNECTIONS,
						notifiers, multicast, group, wordle);
				socket.register(selector, SelectionKey.OP_READ, attachment);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// gestione delle operazioni sui channel
	public void multiplex() {
		try {
			selector.select();
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			SelectionKey key;
			while (it.hasNext()) {
				key = it.next();
				it.remove();
				if (key.isValid()) {
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isWritable()) {
						key.interestOps(0);
						pool.execute(new Sender(key));
					} else if (key.isReadable()) {
						key.interestOps(0);
						pool.execute(new Receiver(key));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean isRunning() {
		return RUNNING;
	}

	public int getActiveConnections() {
		return ACTIVE_CONNECTIONS.get();
	}

	public void shutdown() {
		try {
			System.out.println("< ---------- WORDLE CLOSURE ----------");

			// attende la terminazione di tutti i task avviati
			pool.shutdown();
			while (!pool.awaitTermination(60L, TimeUnit.SECONDS))
				System.out.println("< waiting for thread closure");

			// effettua il backup degli utenti
			json_wrapper.writeArray(users);
			System.out.println("< BACKUP DONE");

			// interruzione thread estrattore di wordle
			extractor.interrupt();
			extractor.join();
			System.out.println("< EXTRACTOR CLOSE");

			// rimozione oggetto RMI remoto
			registry.unbind(Registration.SERVICE);
			UnicastRemoteObject.unexportObject(registration, false);
			System.out.println("< RMI REGISTRATION SERVICE CLOSE");

			// chiusura connessioni TCP
			for (SelectionKey k : selector.keys()) {
				k.channel().close();
			}
			selector.close();
			System.out.println("< TCP CLOSE");

			// chiusura connessione multicast
			multicast.close();
			System.out.println("< MULTICAST CLOSE");

			System.out.println("< ------------------------------------");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
