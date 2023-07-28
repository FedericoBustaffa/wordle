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
	private ByteBuffer buffer;
	private AtomicInteger ACTIVE_CONNECTIONS;

	// MULTICAST
	MulticastSocket multicast;
	SocketAddress group;

	public Sender(List<Notify> notifiers, Selector selector, SocketChannel socket, ByteBuffer buffer,
			AtomicInteger ACTIVE_CONNECTIONS, MulticastSocket multicast, SocketAddress group) {
		this.notifiers = notifiers;

		this.selector = selector;
		this.socket = socket;
		this.buffer = buffer;
		this.ACTIVE_CONNECTIONS = ACTIVE_CONNECTIONS;

		this.multicast = multicast;
		this.group = group;
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

			ACTIVE_CONNECTIONS.decrementAndGet();
			System.out.println("< client has left: " + ACTIVE_CONNECTIONS.get() + " connections");
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			byte[] bytes = buffer.array();
			int length = buffer.limit();
			buffer.flip();
			while (buffer.hasRemaining())
				socket.write(buffer);

			String msg = new String(bytes, 0, length);
			if (!msg.contains("ERROR")) {
				if (msg.contains("share")) {
					this.share(msg);
				} else if (msg.contains("logout")) {
					this.logout(msg);
				} else if (msg.contains("exit")) {
					this.exit(msg);
					selector.wakeup();
					return;
				}
			}

			socket.register(selector, SelectionKey.OP_READ, buffer);
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
