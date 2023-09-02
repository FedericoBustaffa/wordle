import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration extends Remote {

	public static final String SERVICE = "REGISTRATION";

	/**
	 * registra un nuovo utente aggiungendo alla tabella degli utenti
	 * 
	 * @param username
	 * @param password
	 * @return messaggio sullo stato dell'operazione
	 * @throws RemoteException
	 */
	public String register(String username, String password) throws RemoteException;

	/**
	 * registra un nuovo utente al servizio di callback
	 * 
	 * @param notify
	 * @throws RemoteException
	 */
	public void registerForNotification(Notify notify) throws RemoteException;

	/**
	 * rimuove un utente dal servizio di callback
	 * 
	 * @param notify
	 * @throws RemoteException
	 */
	public void unregisterForNotification(Notify notify) throws RemoteException;

}
