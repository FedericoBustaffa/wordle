import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;

public class MulticastReceiver implements Runnable {

	private MulticastSocket multicast;
	private BlockingQueue<String> scores;

	public MulticastReceiver(MulticastSocket multicast, BlockingQueue<String> scores) {
		this.multicast = multicast;
		this.scores = scores;
	}

	public void run() {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[512], 512);
			String msg;
			while (true) {
				multicast.receive(packet);
				msg = new String(packet.getData(), 0, packet.getLength());
				System.out.println(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
