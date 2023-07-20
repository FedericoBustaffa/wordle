import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import java.util.Iterator;
import java.util.Set;

public class Server {

	// CONFIGURATION
	private static final int RMI_PORT = 1500;
	private static final int TCP_PORT = 2000;

	// RMI
	private Registry registry;
	private Registration registration;

	// TCP
	private SocketAddress tcp_service;
	private Selector selector;

	public Server(Set<User> users) {
		try {
			// RMI
			registration = new RegistrationService(users);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration);

			// TCP
			tcp_service = new InetSocketAddress(InetAddress.getLocalHost(), TCP_PORT);
			selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(tcp_service);
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void accept(SelectionKey key) {
		try {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel socket = server.accept();
			socket.configureBlocking(false);
			socket.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(512));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteBuffer buffer = (ByteBuffer) key.attachment();
			buffer.flip();
			while (buffer.hasRemaining())
				socket.write(buffer);

			key.interestOps(SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void receive(SelectionKey key) {
		try {
			SocketChannel socket = (SocketChannel) key.channel();
			ByteBuffer buffer = (ByteBuffer) key.attachment();
			buffer.clear();
			int b = socket.read(buffer);
			String msg = new String(buffer.array(), 0, b);
			System.out.println(msg);
			if (msg.equals("exit")) {
				key.cancel();
				return;
			}
			key.interestOps(SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void multiplex() {
		try {
			selector.select();
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			SelectionKey k;
			while (it.hasNext()) {
				k = it.next();
				it.remove();
				if (k.isAcceptable()) {
					System.out.println("ACCEPT");
					accept(k);
				} else if (k.isReadable()) {
					System.out.println("READ");
					receive(k);
				} else if (k.isWritable()) {
					System.out.println("WRITE");
					send(k);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			// RMI closure
			registry.unbind(Registration.SERVICE);
			UnicastRemoteObject.unexportObject(registration, false);

			// TCP closure
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}
