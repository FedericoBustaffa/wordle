import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientStress {

	// UTILITY
	private String username; // null if not logged
	private int last_score;

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
	private InputStream reader;
	private OutputStream writer;

	// MULTICAST
	private MulticastSocket multicast;
	private InetAddress group;
	private MulticastReceiver mc_receiver;
	private ConcurrentLinkedQueue<String> scores;

	public ClientStress() {
		try {
			username = null;
			last_score = -1;

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

			scores = new ConcurrentLinkedQueue<String>();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			socket.connect(tcp_address);
			reader = socket.getInputStream();
			writer = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
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

	private void login(String cmd) {
		if (username != null) {
			System.out.println("< logout before login");
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
				mc_receiver = new MulticastReceiver(multicast, scores, username);
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
		if (!response.contains("ERROR"))
			last_score = Integer.parseInt(response.split(" ")[2]);
	}

	private void share(String cmd) {
		this.send(cmd + " " + username + " " + last_score);
		String response = this.receive();
		System.out.println("< " + response);
	}

	private void showMeSharing() {
		synchronized (scores) {
			if (scores.size() == 0) {
				System.out.println("< there are no notifications");
				return;
			}

			System.out.println("---- SCORES ----");
			for (String s : scores)
				System.out.println("< " + s);
			System.out.println("----------------");
		}
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
				scores.clear();
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

	public void exit(String cmd) {
		try {
			this.send(cmd + " " + username);
			String response = this.receive();
			System.out.println("< " + response);
			if (!response.contains("ERROR")) {
				if (username != null) {
					username = null;
					UnicastRemoteObject.unexportObject(notify_service, false);
					mc_receiver.join();
					multicast.leaveGroup(group);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shell(String username, String password) {
		try {
			this.login("login " + username + " " + password);
			for (int i = 0; i < 1000; i++) {
				Thread.sleep(10L);
				this.play("play");
				this.share("share");
				this.showMeSharing();
			}
			this.logout("logout");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			// TCP closure
			socket.close();

			// MULTICAST closure
			multicast.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ClientStress client = new ClientStress();
		client.connect();
		client.shell(args[0], args[1]);
		client.shell(args[2], args[3]);
		client.exit("exit");
		client.shutdown();
	}

}