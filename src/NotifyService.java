import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NotifyService extends UnicastRemoteObject implements Notify {

	private String username;
	private String[] top_three;

	public NotifyService(String username) throws RemoteException {
		this.username = username;
		this.top_three = new String[3];
		for (int i = 0; i < this.top_three.length; i++)
			this.top_three[i] = "";
	}

	@Override
	public synchronized String getUsername() throws RemoteException {
		return username;
	}

	@Override
	public synchronized void setTopThree(String top_three) throws RemoteException {
		String[] users = top_three.split(" ");
		for (int i = 0; i < users.length; i++)
			this.top_three[i] = users[i];
	}

	@Override
	public synchronized void update(String top_three) throws RemoteException {
		String[] users = top_three.split(" ");
		System.out.println("\n< ---------- RANKING UPDATE ----------");
		for (int i = 0; i < users.length; i++) {
			if (!this.top_three[i].equals(users[i])) {
				System.out.printf("< %s now at place %d\n", users[i], (i + 1));
				this.top_three[i] = users[i];
			}
		}
		System.out.printf("< ------------------------------------\n> ");
	}

}
