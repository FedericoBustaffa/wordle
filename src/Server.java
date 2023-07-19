import java.io.File;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Server {

	// social network and game data
	private Wordle wordle;

	// configuration values
	private static final String BACKUP_USERS = "users.json";
	private static final int RMI_PORT = 2000;

	// Json backup
	private File backup;
	private ObjectMapper mapper;
	private JsonFactory factory;

	// RMI
	private Registry registry;
	private Registration registration_service;

	public Server() {
		try {
			// wordle init
			wordle = new Wordle();

			// Json
			getUsersBackup();

			registration_service = new RegistrationService(wordle);
			registry = LocateRegistry.createRegistry(RMI_PORT);
			registry.rebind(Registration.SERVICE, registration_service);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void getUsersBackup() {
		try {
			backup = new File(BACKUP_USERS);
			mapper = new ObjectMapper();
			factory = new JsonFactory();
			if (backup.exists()) {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);

				JsonParser parser;
				parser = factory.createParser(backup);
				parser.setCodec(mapper);
				if (parser.nextToken() != JsonToken.START_ARRAY) {
					System.out.println("< users backup file error");
					System.exit(1);
				}
				while (parser.nextToken() == JsonToken.START_OBJECT) {
					wordle.add(parser.readValueAs(User.class));
				}
				parser.close();

				for (User u : wordle.getUsers()) {
					System.out.println(u.toString());
				}
			} else {
				backup.createNewFile();
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}

	public void shutdown() {
		try {
			// creazione file backup json
			JsonGenerator generator = factory.createGenerator(backup, JsonEncoding.UTF8);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();
			generator.writeStartArray();
			for (User u : wordle.getUsers()) {
				generator.writeObject(u);
			}
			generator.writeEndArray();
			generator.close();

			UnicastRemoteObject.unexportObject(registration_service, false);
			registry.unbind(Registration.SERVICE);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		server.shutdown();
	}
}
