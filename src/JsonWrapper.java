import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonWrapper {

    private File file;
    private JsonFactory factory;
    private ObjectMapper mapper;
    private JsonGenerator generator;
    private JsonParser parser;

    public JsonWrapper(String filepath) {
        try {
            if (filepath != null) {
                file = new File(filepath);
                if (!file.exists())
                    file.createNewFile();
            }
            factory = new JsonFactory();
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonWrapper() {
        this(null);
    }

    public void writeArray(ConcurrentHashMap<String, User> users) {
        try {
            generator = factory.createGenerator(file, JsonEncoding.UTF8);
            generator.setCodec(mapper);
            generator.useDefaultPrettyPrinter();
            generator.writeStartArray();
            for (User u : users.values()) {
                generator.writeObject(u);
            }
            generator.writeEndArray();
            generator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, User> readArray() {
        try {
            ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
            parser = factory.createParser(file);
            parser.setCodec(mapper);

            JsonToken token = parser.nextToken();
            // file malformato o vuoto
            if (token != JsonToken.START_ARRAY)
                return users;

            User user;
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                user = parser.readValueAs(User.class);
                users.put(user.getUsername(), user);
            }

            parser.close();

            return users;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getNode(String content, String node) {
        try {
            return mapper.readTree(content).get(node).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
