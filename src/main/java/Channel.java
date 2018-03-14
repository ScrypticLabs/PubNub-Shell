import java.util.List;

/**
 * Created by Abhi on 2018-03-12.
 */
public class Channel {
    public String name;
    public String password;
    public List<String> members;   // new members cannot join channel

    public Channel(String name, String password, List<String> members) {
        this.name = name;
        this.password = password;
        this.members = members;
    }

    public String getName() {
        return this.name;
    }

}
