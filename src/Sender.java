import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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

	public void run() {
		try {
			byte[] bytes = stream.getBytes();
			String response = new String(bytes);
			if (!response.contains("ERROR")) {
				if (response.contains("score")) {
					for (Notify notify : notifiers) {
						if (!notify.getUsername().equals(response.split(" ")[2]))
							notify.update("< " + response);
					}
				}
				if (response.contains("share") ||
						response.contains("logout") ||
						response.contains("exit")) {
					DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, group);
					multicast.send(packet);
				}
			}
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			while (buffer.hasRemaining())
				socket.write(buffer);
			String msg = new String(bytes, 0, bytes.length);
			if (msg.contains("exit success")) {
				stream.close();
				ACTIVE_CONNECTIONS.decrementAndGet();
				System.out.println("< client has left");
				selector.wakeup();
				return;
			}
			socket.register(selector, SelectionKey.OP_READ, stream);
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
