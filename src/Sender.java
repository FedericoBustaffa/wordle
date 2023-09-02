import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender implements Runnable {

	// Attachment
	private SelectionKey key;
	private Attachment attachment;

	// classifica
	private List<User> ranking;

	// TCP
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer buffer;
	private volatile AtomicInteger ACTIVE_CONNECTIONS;

	// RMI notifiers
	private List<Notify> notifiers;

	// MULTICAST
	MulticastSocket multicast;
	SocketAddress group;

	public Sender(SelectionKey key) {
		this.key = key;
		this.attachment = (Attachment) key.attachment();

		this.ranking = attachment.getRanking();

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
			if (!msg.contains("ERROR")) {
				msg = msg.substring(15);
				DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
				multicast.send(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// aggiorna la classifica e nel caso ci siano variazioni nei primi 3 posti invia
	// una notifica agli utenti online
	private void updateRanking() {
		try {
			String[] top_three;
			if (ranking.size() >= 3)
				top_three = new String[3];
			else
				top_three = new String[ranking.size()];

			for (int i = 0; i < top_three.length; i++)
				top_three[i] = ranking.get(i).getUsername();

			Collections.sort(ranking);
			String username;
			String msg = "";
			boolean send = false;
			for (int i = 0; i < top_three.length; i++) {
				username = ranking.get(i).getUsername();
				msg = msg + username + " ";
				if (!username.equals(top_three[i]))
					send = true;
			}

			if (send) {
				for (int j = 0; j < notifiers.size(); j++)
					notifiers.get(j).update(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// invia un messaggio di logout al thread del client in ascolto sul
	// gruppo multicast
	private void logout(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// invia un messaggio di logout al thread del client in ascolto sul
	// gruppo multicast e decrementa il numero di connessioni attive
	private void exit(String msg) {
		try {
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), 0, msg.length(), group);
			multicast.send(packet);
			synchronized (ACTIVE_CONNECTIONS) {
				System.out.println("< client has left: " +
						ACTIVE_CONNECTIONS.decrementAndGet() + " clients connected");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			buffer.flip();
			byte[] bytes = buffer.array();
			int length = buffer.limit();
			while (buffer.hasRemaining())
				socket.write(buffer);

			String msg = new String(bytes, 0, length);
			if (!msg.contains("ERROR")) {
				if (msg.contains("guess right"))
					this.updateRanking();
				else if (msg.contains("share"))
					this.share(msg);
				else if (msg.contains("logout"))
					this.logout(msg);
				else if (msg.contains("exit")) {
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
