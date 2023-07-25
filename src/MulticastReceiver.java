import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;

public class MulticastReceiver extends Thread {

	private InetAddress group;
	private MulticastSocket multicast;
	private BlockingQueue<String> scores;
	private String username;

	public MulticastReceiver(InetAddress group, MulticastSocket multicast,
			BlockingQueue<String> scores, String username) {
		this.group = group;
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
				if (msg.contains("leave")) {
					if (msg.contains(username)) {
						multicast.leaveGroup(group);
						break;
					}
				} else if (!msg.contains(username)) {
					System.out.printf("\n< " + msg + "\n> ");
					scores.add(msg);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
