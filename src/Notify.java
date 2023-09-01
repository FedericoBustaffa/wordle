import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify extends Remote {

	public String getUsername() throws RemoteException;

	public void setTopThree(String top_three) throws RemoteException;

	public void update(String top_three) throws RemoteException;

}
