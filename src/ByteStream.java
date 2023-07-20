import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ByteStream {

	private ByteArrayInputStream byte_reader;
	private ByteArrayOutputStream byte_writer;
	private DataInputStream data_reader;
	private DataOutputStream data_writer;
	private ObjectInputStream object_reader;
	private ObjectOutputStream object_writer;

	public ByteStream(int size) {
		try {
			byte_reader = new ByteArrayInputStream(new byte[size]);
			byte_writer = new ByteArrayOutputStream();
			data_reader = new DataInputStream(byte_reader);
			data_writer = new DataOutputStream(byte_writer);
			object_reader = new ObjectInputStream(byte_reader);
			object_writer = new ObjectOutputStream(byte_writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		return byte_writer.toByteArray();
	}

	public void reset() {
		byte_writer.reset();
	}

	public void write(String s) {
		try {
			data_writer.writeUTF(s);
			data_writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String read() {
		try {
			byte_reader.read(byte_writer.toByteArray());
			return data_reader.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void writeUser(User user) {
		try {
			object_writer.writeObject(user);
			object_writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public User readUser() {
		try {
			return (User) object_reader.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
