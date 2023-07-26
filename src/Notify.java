import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify extends Remote {

	public String getUsername() throws RemoteException;

	public void update(String msg) throws RemoteException;

}
