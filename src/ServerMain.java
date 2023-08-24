public class ServerMain {
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		while (server.isRunning() || server.getActiveConnections() > 0) {
			server.multiplex();
		}
		server.shutdown();
	}
}