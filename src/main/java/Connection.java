/**
 * Created by Abhi on 2018-03-12.
 */
public class Connection {
    private String user;        // Username
    private String address;     // Channel Name
    private String password;
    private String id;

    public Connection(User user, String username, String address, String password, boolean client) {
        this.user = username;
        this.address = address;
        this.password = password;
        if (client) {
            this.id = this.user+"."+user.getUsername()+"."+this.address; // to.from.channel to use the wildcard pubnub subscription method on to.*
        } else {
            this.id = this.address; // from.to.channel to reply back to message
        }
    }

    public String getUser() {
        return this.user;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPassword() {
        return this.password;
    }

    public String getID() {
        return this.id;
    }
}
