import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;

/**
 * Created by Abhi on 2018-03-13.
 */

public class PubNubMessage {
    private String publisher;
    private String message;
    private JsonObject json;

    public PubNubMessage(String publisher, String message) {
        this.publisher = publisher;
        this.message = message;
        this.json = new JsonObject();
        this.json.addProperty("from", this.publisher);
        this.json.addProperty("message", this.message);
    }

    public PubNubMessage(JsonArray json) {
        JsonObject JSON = json.get(0).getAsJsonObject();
        this.publisher = JSON.get("from").toString();
        this.publisher = this.publisher.substring(1, this.publisher.length()-1);
        this.message = JSON.get("message").toString();
        this.message = this.message.substring(1, this.message.length()-1);
        this.json = JSON;
    }

    public JsonObject getJSON() {
        return this.json;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.json.toString();
    }
}
