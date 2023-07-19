import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration extends Remote {

	public static final String SERVICE = "REGISTRATION";

	public void register(String username, String password) throws RemoteException;

}
