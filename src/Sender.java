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

	// Attachment
	private SelectionKey key;
	private Attachment attachment;

	// TCP
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer buffer;
	private AtomicInteger ACTIVE_CONNECTIONS;

	// RMI notifiers
	private List<Notify> notifiers;

	// MULTICAST
	MulticastSocket multicast;
	SocketAddress group;

	public Sender(SelectionKey key) {
		this.key = key;
		this.attachment = (Attachment) key.attachment();

		this.selector = key.selector();
		this.socket = (SocketChannel) key.channel();
		this.buffer = attachment.getBuffer();
		this.ACTIVE_CONNECTIONS = attachment.getActiveConnections();

		this.notifiers = attachment.getNotifiers();

		this.multicast = attachment.getMulticast();
		this.group = attachment.getGroup();
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

			System.out.println("< client has left: " +
					ACTIVE_CONNECTIONS.decrementAndGet() + " connections");
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

			key.interestOps(SelectionKey.OP_READ);
			key.attach(attachment);
			selector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
