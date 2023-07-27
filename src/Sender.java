import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender implements Runnable {

	// RMI notifiers
	private List<Notify> notifiers;

	// TCP
	private Selector selector;
	private SocketChannel socket;
	private ByteStream stream;
	private AtomicInteger ACTIVE_CONNECTIONS;

	// MULTICAST
	MulticastSocket multicast;
	SocketAddress group;

	public Sender(List<Notify> notifiers, Selector selector, SocketChannel socket, ByteStream stream,
			AtomicInteger ACTIVE_CONNECTIONS, MulticastSocket multicast, SocketAddress group) {
		this.notifiers = notifiers;

		this.selector = selector;
		this.socket = socket;
		this.stream = stream;
		this.ACTIVE_CONNECTIONS = ACTIVE_CONNECTIONS;

		this.multicast = multicast;
		this.group = group;
	}

	private void help() {
		String commands = "--- HELP ---\n" +
				"< register <username> <password>\n" +
				"< login <username> <password>\n" +
				"< play\n" +
				"< share\n" +
				"< show\n" +
				"< logout\n" +
				"< exit";

		stream.write(commands);
	}

	private void play(String msg) {
		try {
			String username = msg.split(" ")[1];
			int score = Integer.parseInt(msg.split(" ")[2]);

			for (Notify notifier : notifiers) {
				if (!username.equals(notifier.getUsername()))
					notifier.update(username + ": " + score);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private void share(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logout(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exit(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
			stream.close();
			ACTIVE_CONNECTIONS.decrementAndGet();
			System.out.println("< client has left: " + ACTIVE_CONNECTIONS.get() + " connections");
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg = new String(stream.getBytes());
			if (!msg.contains("ERROR")) {
				if (msg.contains("help")) {
					this.help();
				} else if (msg.contains("play")) {
					this.play(msg);
				} else if (msg.contains("share")) {
					this.share(msg);
				} else if (msg.contains("logout")) {
					this.logout(msg);
				} else if (msg.contains("exit")) {
					this.exit(msg);
					return;
				}
			}

			ByteBuffer buffer = ByteBuffer.wrap(stream.getBytes());
			while (buffer.hasRemaining())
				socket.write(buffer);

			socket.register(selector, SelectionKey.OP_READ, stream);
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
