import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.List;

public class MulticastReceiver extends Thread {

	private MulticastSocket multicast;
	private List<String> sessions;
	private String username;

	public MulticastReceiver(MulticastSocket multicast, List<String> sessions, String username) {
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
					// se nel messaggio di logout o exit è presente lo username
					// dell'utente allora il thread si arresta
					if (msg.contains(username))
						break;
				} else if (!msg.contains(username) && !sessions.contains(msg)) {
					// aggiunta del messaggio alla struttura dati
					sessions.add(msg);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
