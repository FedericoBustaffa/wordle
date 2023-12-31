import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonWrapper {

    private File file;
    private JsonFactory factory;
    private ObjectMapper mapper;
    private JsonGenerator generator;
    private JsonParser parser;

    public JsonWrapper(File file) {
        this.file = file;
        factory = new JsonFactory();
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public JsonWrapper() {
        this(null);
    }

    // scrive la lista utenti sul file di backup
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

    // legge la lista utenti dal file di backup
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

    // legge il contenuto di un file JSON e lo converte in stringa
    public String getContent() {
        try {
            if (file == null)
                return null;
            else
                return mapper.readTree(file).toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Legge la stringa <content> (file json in forma di stringa) e ne estrae il
     * valore relativo al campo <field>
     * 
     * @param content
     * @param field
     * @return il valore di tipo stringa corrispondente al campo <field> se presente
     * @throws NoSuchElementException se il campo non <field> non è presente
     */
    public String getString(String content, String field) throws NoSuchElementException {
        try {
            JsonNode node = mapper.readTree(content);
            if (node.has(field)) {
                return node.get(field).asText();
            } else if (node.isObject()) {
                for (JsonNode n : node)
                    return getString(n.toString(), field);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NoSuchElementException(field);
    }

    /**
     * @param content
     * @param field
     * @return il valore di tipo intero corrispondente al campo <field> se presente.
     * @throws NoSuchElementException se il campo non <field> non è presente
     */
    public int getInteger(String content, String field) throws NoSuchElementException {
        try {
            JsonNode node = mapper.readTree(content);
            if (node.has(field)) {
                return node.get(field).asInt();
            } else if (node.isObject()) {
                for (JsonNode n : node)
                    return getInteger(n.toString(), field);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NoSuchElementException(field);
    }

    /**
     * @param content
     * @param field
     * @return il valore di tipo long corrispondente al campo <field> se presente.
     * @throws NoSuchElementException se il campo non <field> non è presente
     */
    public long getLong(String content, String field) throws NoSuchElementException {
        try {
            JsonNode node = mapper.readTree(content);
            if (node.has(field)) {
                return node.get(field).asInt();
            } else if (node.isObject()) {
                for (JsonNode n : node)
                    return getInteger(n.toString(), field);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NoSuchElementException(field);
    }

}
