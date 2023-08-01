import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MulticastReceiver extends Thread {

	private MulticastSocket multicast;
	private ConcurrentLinkedQueue<String> scores;
	private String username;

	public MulticastReceiver(MulticastSocket multicast, ConcurrentLinkedQueue<String> scores,
			String username) {
		this.multicast = multicast;
		this.scores = scores;
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
				} else if (!msg.contains(username))
					scores.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
