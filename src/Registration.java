import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration extends Remote {

	public static final String SERVICE = "REGISTRATION";

	public String register(String username, String password) throws RemoteException;

	public void registerForNotification(Notify notify) throws RemoteException;

	public void unregisterForNotification(Notify notify) throws RemoteException;

}
