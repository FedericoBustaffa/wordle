import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUser {

    private File file;
    private JsonFactory factory;
    private ObjectMapper mapper;
    private JsonGenerator generator;
    private JsonParser parser;

    public JsonUser(String filepath) {
        file = new File(filepath);
        factory = new JsonFactory();
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void writeArray(Set<User> users) {
        try {
            generator = factory.createGenerator(file, JsonEncoding.UTF8);
            generator.setCodec(mapper);
            generator.useDefaultPrettyPrinter();
            generator.writeStartArray();
            for (User u : users) {
                generator.writeObject(u);
            }
            generator.writeEndArray();
            generator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<User> readArray() {
        try {
            Set<User> objects = new TreeSet<User>();
            parser = factory.createParser(file);
            parser.setCodec(mapper);

            if (parser.nextToken() != JsonToken.START_ARRAY)
                return null;

            while (parser.nextToken() == JsonToken.START_OBJECT)
                objects.add(parser.readValueAs(User.class));

            parser.close();

            return objects;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
