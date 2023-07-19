import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private Wordle wordle;

	public RegistrationService(Wordle wordle) throws RemoteException {
		this.wordle = wordle;
	}

	@Override
	public void register(String username, String password) throws RemoteException {
		if (username == null || username.equals("")) {
			System.out.println("< invalid username");
			return;
		} else if (password == null || password.equals("")) {
			System.out.println("invalid password");
			return;
		}

		synchronized (wordle) {
			if (!wordle.add(new User(username, password))) {
				System.out.println("< username: " + username + " is already taken");
			} else {
				System.out.println("< user " + username + " registered");
			}

		}
	}
}
