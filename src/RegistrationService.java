import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private Set<User> users;

	public RegistrationService(Set<User> users) throws RemoteException {
		this.users = users;
	}

	@Override
	public String register(String username, String password) throws RemoteException {
		if (username == null || username.equals("")) {
			return "< invalid username";
		} else if (password == null || password.equals("")) {
			return "< invalid password";
		}

		if (!users.add(new User(username, password))) {
			return "< username \"" + username + "\" not available";
		} else {
			System.out.println("< new user: " + username + " has been registered");
			return "< successful registration";
		}
	}
}
