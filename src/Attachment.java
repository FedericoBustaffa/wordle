import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Attachment {

	private ByteBuffer buffer;
	private ConcurrentHashMap<String, User> users;
	private List<User> ranking;
	private volatile AtomicInteger ACTIVE_CONNECTIONS;
	private List<Notify> notifiers;
	private MulticastSocket multicast;
	private SocketAddress group;
	private Wordle wordle;

	public Attachment(ByteBuffer buffer, ConcurrentHashMap<String, User> users, List<User> ranking,
			AtomicInteger ACTIVE_CONNECTIONS, List<Notify> notifiers, MulticastSocket multicast,
			SocketAddress group, Wordle wordle) {
		this.buffer = buffer;
		this.users = users;
		this.ranking = ranking;
		this.ACTIVE_CONNECTIONS = ACTIVE_CONNECTIONS;
		this.notifiers = notifiers;
		this.multicast = multicast;
		this.group = group;
		this.wordle = wordle;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public ConcurrentHashMap<String, User> getUsers() {
		return users;
	}

	public List<User> getRanking() {
		return ranking;
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
