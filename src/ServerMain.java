public class ServerMain {
	public static void main(String[] args) {
		try {
			if (args.length != 1) {
				System.out.println("< USAGE: java ServerMain <config_file>");
				System.exit(1);
			}

			Server server = new Server(args[0]);
			server.start(); // thread per la chiusura del server
			while (server.isRunning() || server.getActiveConnections() > 0)
				server.multiplex();

			server.join();
			server.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}