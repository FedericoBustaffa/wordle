import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private ConcurrentHashMap<String, User> users;
	private List<User> ranking;
	private List<Notify> notify_services;

	public RegistrationService(ConcurrentHashMap<String, User> users, List<User> ranking,
			List<Notify> notify_services)
			throws RemoteException {
		this.users = users;
		this.ranking = ranking;
		this.notify_services = notify_services;
	}

	@Override
	public synchronized String register(String username, String password) throws RemoteException {
		if (username == null || username.equals("")) {
			return "< invalid username";
		} else if (password == null || password.equals("")) {
			return "< invalid password";
		}

		User user = new User(username, password);
		if (users.get(username) != null) {
			return "< username \"" + username + "\" not available";
		} else {
			users.put(username, user);
			ranking.add(user);
			System.out.println("< new user: " + username + " has been registered");
			return "< successful registration";
		}
	}

	@Override
	public synchronized void registerForNotification(Notify notify) throws RemoteException {
		notify_services.add(notify);
	}

	@Override
	public synchronized void unregisterForNotification(Notify notify) throws RemoteException {
		notify_services.remove(notify);
	}

}
