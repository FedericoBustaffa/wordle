import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteStream {

	private ByteArrayOutputStream baos;

	public ByteStream(int size) {
		baos = new ByteArrayOutputStream();
	}

	public byte[] getBytes() {
		byte[] bytes = baos.toByteArray();
		baos.reset();
		return bytes;
	}

	public void write(String s) {
		try (DataOutputStream os = new DataOutputStream(baos)) {
			os.writeUTF(s);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
