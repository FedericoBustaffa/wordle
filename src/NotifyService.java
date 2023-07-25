import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NotifyService extends UnicastRemoteObject implements Notify {

	public NotifyService() throws RemoteException {
		super();
	}

	@Override
	public void update(String msg) throws RemoteException {
		System.out.printf("\n< " + msg + "\n> ");
	}

}
