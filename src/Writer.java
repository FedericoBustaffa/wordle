import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Writer implements Runnable {

	private Selector selector;
	private SocketChannel socket;
	private ByteStream stream;
	private AtomicInteger ACTIVE_CONNECTIONS;

	public Writer(Selector selector, SocketChannel socket,
			ByteStream stream, AtomicInteger ACTIVE_CONNECTIONS) {
		this.selector = selector;
		this.socket = socket;
		this.stream = stream;
		this.ACTIVE_CONNECTIONS = ACTIVE_CONNECTIONS;
	}

	public void run() {
		try {
			byte[] bytes = stream.getBytes();
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
