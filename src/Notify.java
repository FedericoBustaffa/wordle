import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify extends Remote {

	/**
	 * @return il nome utente relativo a questo oggetto
	 * @throws RemoteException
	 */
	public String getUsername() throws RemoteException;

	/**
	 * aggiorna la struttura dati contenente i primi tre utenti in classifica
	 * 
	 * @param top_three
	 * @throws RemoteException
	 */
	public void setTopThree(String top_three) throws RemoteException;

	/**
	 * invia la nuova top 3 al client
	 * 
	 * @param top_three
	 * @throws RemoteException
	 */
	public void update(String top_three) throws RemoteException;

}
