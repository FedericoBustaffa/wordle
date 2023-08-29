import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client extends Thread {

	// UTILITY
	private String username; // null if not logged
	private boolean done;
	private Scanner input;
	private JsonWrapper json_wrapper;

	// CONFIGURATION
	private String RMI_ADDRESS;
	private int RMI_PORT;
	private String TCP_ADDRESS;
	private int TCP_PORT;
	private String MULTICAST_ADDRESS;
	private int MULTICAST_PORT;

	// RMI
	private Registry registry;
	private Registration registration;
	private Notify notify_service;

	// TCP
	private SocketAddress tcp_address;
	private Socket socket;
	private InputStream reader;
	private OutputStream writer;

	// MULTICAST
	private MulticastSocket multicast;
	private InetAddress group;
	private MulticastReceiver mc_receiver;
	private List<String> sessions;

	public Client(String config_file) {
		try {
			username = null;
			done = false;
			input = new Scanner(System.in);
			json_wrapper = new JsonWrapper();

			// configuration file parsing
			File config = new File(config_file);
			if (!config.exists()) {
				System.out.println("ERROR: server configuration file not found");
				System.exit(1);
			}
			json_wrapper = new JsonWrapper(config);
			String conf = json_wrapper.getContent();

			RMI_ADDRESS = json_wrapper.getString(conf, "rmi_address");
			RMI_PORT = json_wrapper.getInteger(conf, "rmi_port");
			TCP_ADDRESS = json_wrapper.getString(conf, "tcp_address");
			TCP_PORT = json_wrapper.getInteger(conf, "tcp_port");
			MULTICAST_ADDRESS = json_wrapper.getString(conf, "multicast_address");
			MULTICAST_PORT = json_wrapper.getInteger(conf, "multicast_port");

			// RMI
			registry = LocateRegistry.getRegistry(RMI_ADDRESS, RMI_PORT);
			registration = (Registration) registry.lookup(Registration.SERVICE);

			// TCP
			InetAddress remote_tcp;
			if (TCP_ADDRESS.equals("localhost"))
				remote_tcp = InetAddress.getLocalHost();
			else
				remote_tcp = InetAddress.getByName(TCP_ADDRESS);
			tcp_address = new InetSocketAddress(remote_tcp, TCP_PORT);
			socket = new Socket();

			// MULTICAST
			multicast = new MulticastSocket(MULTICAST_PORT);
			group = InetAddress.getByName(MULTICAST_ADDRESS);
			sessions = Collections.synchronizedList(new LinkedList<String>());

			// ShutdownHook for SIGINT capture
			Runtime.getRuntime().addShutdownHook(this);
		} catch (ConnectException e) {
			System.out.println("< server not online\n< shutting down");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("< invalid client configuration file" +
					e.getLocalizedMessage() + " field not found");
			System.exit(1);
		}
	}

	@Override
	public void run() {
		// code executed in case of SIGINT
		this.exit("exit");
	}

	public void connect() {
		try {
			socket.connect(tcp_address);
			reader = socket.getInputStream();
			writer = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("< CONNECTION ERROR: server not online");
			System.exit(1);
		}
	}

	public void send(String cmd) {
		try {
			writer.write(cmd.getBytes(), 0, cmd.length());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String receive() {
		try {
			byte[] buffer = new byte[512];
			int bytes = reader.read(buffer);
			return new String(buffer, 0, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void help(String cmd) {
		this.send(cmd);
		String response = this.receive();
		System.out.println("< " + response);
	}

	public String register(String cmd) {
		try {
			String[] parse = cmd.split(" ");
			if (parse.length != 3)
				return "< ERROR USAGE: register <username> <password>";

			return registration.register(parse[1], parse[2]);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void login(String cmd) {
		if (username != null) {
			System.out.println("< you are already logged in");
			return;
		}

		try {
			this.send(cmd);
			String response = this.receive();
			System.out.println("< " + response);
			if (!response.contains("ERROR")) {
				username = cmd.split(" ")[1];

				notify_service = new NotifyService(username);
				registration.registerForNotification(notify_service);

				multicast.joinGroup(group);
				mc_receiver = new MulticastReceiver(multicast, sessions, username);
				mc_receiver.start();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void play(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println("< " + response);
	}

	private void translate(String word) {
		try {
			URL url = new URL("https://api.mymemory.translated.net/get?q=" +
					word + "&langpair=en|it");
			HttpURLConnection translator = (HttpURLConnection) url.openConnection();
			translator.setRequestMethod("GET");
			BufferedInputStream is = new BufferedInputStream(translator.getInputStream());
			byte[] buffer = new byte[translator.getContentLength()];
			int count = is.read(buffer);
			String content = new String(buffer, 0, count);
			String translation = json_wrapper.getString(content, "translatedText");
			System.out.println("< translation: " + translation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void guess(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println("< " + response);
		if (response.contains("right") || response.contains("attempts terminated for")) {
			String[] split = response.split(" ");
			String word = split[split.length - 1];
			translate(word);
		}
	}

	private void statistics(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println("< ----- STATISTICS -----\n< " + response);
	}

	private void share(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println("< " + response);
	}

	private void show() {
		if (sessions.size() == 0) {
			System.out.println("< there are no notifications");
			return;
		}

		System.out.println("< ---- sessions ----");
		for (String s : sessions)
			System.out.println("< " + s);
		System.out.println("< ----------------");
	}

	private void ranking(String cmd) {
		this.send(cmd);
		String response = this.receive();
		System.out.println("< " + response);
	}

	private void logout(String cmd) {
		try {
			this.send(cmd + " " + username);
			String response = this.receive();
			System.out.println("< " + response);
			if (!response.contains("ERROR")) {
				username = null;

				registration.unregisterForNotification(notify_service);
				UnicastRemoteObject.unexportObject(notify_service, false);
				notify_service = null;

				mc_receiver.join();
				multicast.leaveGroup(group);
				sessions.clear();

				String[] parse = response.split(" ");
				if (parse.length == 4)
					translate(parse[3]);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exit(String cmd) {
		try {
			this.send(cmd + " " + username);
			String response = this.receive();
			System.out.println("< " + response);
			if (!response.contains("ERROR")) {
				done = true;
				if (username != null) {
					username = null;
					registration.unregisterForNotification(notify_service);
					UnicastRemoteObject.unexportObject(notify_service, false);
					mc_receiver.join();
					multicast.leaveGroup(group);
				}

				String[] parse = response.split(" ");
				if (parse.length == 4)
					translate(parse[3]);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shell() {
		this.connect();
		String cmd;
		String first;
		while (!done) {
			System.out.printf("> ");
			cmd = input.nextLine().trim();
			first = cmd.split(" ")[0];
			if (first.equals("help"))
				this.help(cmd);
			else if (first.equals("register"))
				System.out.println(this.register(cmd));
			else if (first.equals("login"))
				this.login(cmd);
			else if (first.equals("logout"))
				this.logout(cmd);
			else if (first.equals("play"))
				this.play(cmd);
			else if (first.equals("guess"))
				this.guess(cmd);
			else if (first.equals("statistics"))
				this.statistics(cmd);
			else if (first.equals("share"))
				this.share(cmd);
			else if (first.equals("show"))
				this.show();
			else if (first.equals("ranking"))
				this.ranking(cmd);
			else if (first.equals("exit")) {
				Runtime.getRuntime().removeShutdownHook(this);
				this.exit(cmd);
			} else
				System.out.println("< invalid command");
		}
	}

	public void shutdown() {
		try {
			// Scanner closure
			input.close();

			// TCP closure
			socket.close();

			// MULTICAST closure
			multicast.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}