public class ServerMain {
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.start();
			while (server.isRunning() || server.getActiveConnections() > 0)
				server.multiplex();

			server.join();
			server.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}