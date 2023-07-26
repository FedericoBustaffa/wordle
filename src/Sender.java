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

	public void play(String msg) {
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

	public void share(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String msg = new String(stream.getBytes());
			if (!msg.contains("ERROR")) {
				if (msg.contains("play")) {
					play(msg);
				} else if (msg.contains("share")) {
					share(msg);
				}
			}

			if (response.contains("share") ||
					response.contains("logout") ||
					response.contains("exit")) {
				DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, group);
				multicast.send(packet);
			}
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			while (buffer.hasRemaining())
				socket.write(buffer);
			if (response.contains("exit success")) {
				stream.close();
				ACTIVE_CONNECTIONS.decrementAndGet();
				System.out.println("< client has left");
				selector.wakeup();
				return;
			}
			socket.register(selector, SelectionKey.OP_READ, stream);
			selector.wakeup();
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}
}
