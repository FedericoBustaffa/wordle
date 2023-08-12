import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Attachment {

	private ByteBuffer buffer;
	private Set<User> users;
	private volatile AtomicInteger ACTIVE_CONNECTIONS;
	private List<Notify> notifiers;
	private MulticastSocket multicast;
	private SocketAddress group;
	private Wordle wordle;

	public Attachment(ByteBuffer buffer, Set<User> users, AtomicInteger ACTIVE_CONNECTIONS,
			List<Notify> notifiers, MulticastSocket multicast, SocketAddress group,
			Wordle wordle) {
		this.buffer = buffer;
		this.users = users;
		this.ACTIVE_CONNECTIONS = ACTIVE_CONNECTIONS;
		this.notifiers = notifiers;
		this.multicast = multicast;
		this.group = group;
		this.wordle = wordle;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public Set<User> getUsers() {
		return users;
	}

	public AtomicInteger getActiveConnections() {
		return ACTIVE_CONNECTIONS;
	}

	public List<Notify> getNotifiers() {
		return notifiers;
	}

	public MulticastSocket getMulticast() {
		return multicast;
	}

	public SocketAddress getGroup() {
		return group;
	}

	public Wordle getWordle() {
		return wordle;
	}

}
