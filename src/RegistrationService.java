import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private Set<User> users;
	private List<Notify> notify_services;

	public RegistrationService(Set<User> users, List<Notify> notify_services) throws RemoteException {
		this.users = users;
		this.notify_services = notify_services;
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

	@Override
	public void registerForNotification(Notify notify) throws RemoteException {
		notify_services.add(notify);
	}

	@Override
	public void unregisterForNotification(Notify notify) throws RemoteException {
		notify_services.remove(notify);
	}

}
