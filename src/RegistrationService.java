import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrationService extends UnicastRemoteObject implements Registration {

	private ConcurrentHashMap<String, User> users;
	private List<User> ranking;
	private List<Notify> notifiers;

	public RegistrationService(ConcurrentHashMap<String, User> users, List<User> ranking,
			List<Notify> notifiers)
			throws RemoteException {
		this.users = users;
		this.ranking = ranking;
		this.notifiers = notifiers;
	}

	@Override
	public synchronized String register(String username, String password) throws RemoteException {
		if (username == null || username.equals(""))
			return "< invalid username";
		else if (password == null || password.equals(""))
			return "< invalid password";

		User user = new User(username, password);
		if (users.get(username) != null)
			return "< username \"" + username + "\" not available";
		else {
			users.put(username, user);
			ranking.add(user);
			System.out.println("< new user: " + username + " has been registered");
			return "< SUCCESS: registration " + username;
		}
	}

	@Override
	public synchronized void registerForNotification(Notify notify) throws RemoteException {
		StringBuilder builder = new StringBuilder();
		int len;
		if (ranking.size() >= 3)
			len = 3;
		else
			len = ranking.size();

		for (int i = 0; i < len; i++)
			builder.append(ranking.get(i).getUsername() + " ");
		notify.setTopThree(builder.toString());
		notifiers.add(notify);
	}

	@Override
	public synchronized void unregisterForNotification(Notify notify) throws RemoteException {
		notifiers.remove(notify);
	}

}
