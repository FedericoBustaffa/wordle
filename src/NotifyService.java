import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NotifyService extends UnicastRemoteObject implements Notify {

	private String username;

	public NotifyService(String username) throws RemoteException {
		this.username = username;
	}

	@Override
	public String getUsername() throws RemoteException {
		return username;
	}

	@Override
	public void update(String msg) throws RemoteException {
		System.out.printf("\n< " + msg + "\n> ");
	}

}
