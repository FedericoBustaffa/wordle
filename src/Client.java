import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

	// UTILITY
	private String username; // null if not logged
	private boolean done;
	private Scanner input;

	// CONFIGURATION
	private int RMI_PORT = 1500;
	private int TCP_PORT = 2000;
	private String MULTICAST_ADDRESS;
	private int MULTICAST_PORT;

	// RMI
	private Registry registry;
	private Registration registration;
	private Notify notify_service;

	// TCP
	private SocketAddress tcp_address;
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;

	// MULTICAST
	private MulticastSocket multicast;
	private InetAddress group;
	private MulticastReceiver mc_receiver;
	private BlockingQueue<String> scores;

	public Client() {
		try {

			username = null;
			done = false;
			input = new Scanner(System.in);

			// configuration file
			File config = new File("client_config.txt");
			if (!config.exists()) {
				System.out.println("ERROR: server configuration file not found");
				System.exit(1);
			}

			// configuration file parsing
			Scanner scanner = new Scanner(config);
			String[] line;
			while (scanner.hasNext()) {
				line = scanner.nextLine().split("=");
				if (line[0].equals("RMI_PORT"))
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

			// RMI
			registry = LocateRegistry.getRegistry(RMI_PORT);
			registration = (Registration) registry.lookup(Registration.SERVICE);

			// TCP
			tcp_address = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);
			socket = new Socket();

			// MULTICAST
			multicast = new MulticastSocket(MULTICAST_PORT);
			group = InetAddress.getByName(MULTICAST_ADDRESS);

			scores = new LinkedBlockingQueue<String>();
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

	public String receive() {
		try {
			return reader.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void login(String cmd) {
		if (username != null) {
			System.out.println("< logout before login");
			return;
		}

		try {
			this.send(cmd);
			String response = this.receive();
			System.out.println(response);
			if (!response.contains("ERROR")) {
				username = response.split(" ")[3];
				notify_service = new NotifyService();

				multicast.joinGroup(group);

				mc_receiver = new MulticastReceiver(group, multicast, scores, username);
				mc_receiver.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logout(String cmd) {
		try {
			this.send(cmd + " " + username);
			String response = this.receive();
			System.out.println(response);
			if (!response.contains("ERROR")) {
				username = null;
				mc_receiver.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void play(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println(response);
	}

	private void share(String cmd) {
		this.send(cmd + " " + username);
		String response = this.receive();
		System.out.println(response);
	}

	private void exit(String cmd) {
		try {
			this.send(cmd + " " + username);
			String response = this.receive();
			System.out.println(response);
			if (!response.contains("ERROR")) {
				username = null;
				done = true;
				if (mc_receiver != null)
					mc_receiver.join();
			}
		} catch (InterruptedException e) {
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

			if (first.equals("register"))
				System.out.println(this.register(cmd));
			else if (first.equals("login"))
				this.login(cmd);
			else if (first.equals("logout"))
				this.logout(cmd);
			else if (first.equals("play"))
				this.play(cmd);
			else if (first.equals("share"))
				this.share(cmd);
			else if (first.equals("exit"))
				this.exit(cmd);
			else
				System.out.println("< invalid command");
		}
	}

	public void shutdown() {
		try {
			// Scanner closure
			input.close();

			// RMI closure
			if (notify_service != null)
				UnicastRemoteObject.unexportObject(notify_service, false);

			// TCP closure
			socket.close();

			// MULTICAST closure
			multicast.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.shell();
		client.shutdown();
	}

}