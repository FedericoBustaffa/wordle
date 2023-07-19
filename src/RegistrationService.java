import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private Wordle wordle;

	public RegistrationService(Wordle wordle) throws RemoteException {
		this.wordle = wordle;
	}

	@Override
	public String register(String username, String password) throws RemoteException {
		if (username == null || username.equals("")) {
			return "invalid username";
		} else if (password == null || password.equals("")) {
			return "invalid password";
		}

		synchronized (wordle) {
			if (!wordle.add(new User(username, password))) {
				return "username \"" + username + "\" not available";
			} else {
				return "successful registration";
			}

		}
	}
}
