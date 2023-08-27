import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Queue;

public class MulticastReceiver extends Thread {

	private MulticastSocket multicast;
	private Queue<String> sessions;
	private String username;

	public MulticastReceiver(MulticastSocket multicast, Queue<String> sessions, String username) {
		this.multicast = multicast;
		this.sessions = sessions;
		this.username = username;
	}

	public void run() {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[512], 512);
			String msg;
			while (true) {
				multicast.receive(packet);
				msg = new String(packet.getData(), 0, packet.getLength());
				if (msg.contains("logout") || msg.contains("exit")) {
					if (msg.contains(username))
						break;
				} else if (!msg.contains(username) && !sessions.contains(msg))
					sessions.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
