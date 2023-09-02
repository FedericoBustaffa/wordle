public class ClientMain {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("< USAGE: java ClientMain <config_file>");
			System.exit(1);
		}
		Client client = new Client(args[0]);
		client.connect();
		client.shell();
		client.shutdown();
	}
}
