import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import com.google.firebase.database.*;
import com.google.firebase.database.core.SnapshotHolder;
//import com.google.firebase.quickstart.email.MyEmailer;
//import com.google.firebase.quickstart.model.Post;
//import com.google.firebase.quickstart.model.User;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by Abhi on 2018-03-11.
 */

public class Database {

    private static final String DATABASE_URL = "https://pubnub-shell.firebaseio.com/";
    private ClassLoader classLoader = getClass().getClassLoader();
    private static final String SERVICE_ACCOUNT = "pubnub-shell-firebase-adminsdk-uvdrc-a1ef775276.json";
    private static DatabaseReference database;
    private Map<String, String> routes = new HashMap<>();

    public Database() {
        try {
            FileInputStream serviceAccount = new FileInputStream(classLoader.getResource(SERVICE_ACCOUNT).getFile());
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            System.out.println("ERROR: invalid service account credentials. See README.");
            System.out.println(e.getMessage());
            System.exit(1);
        }
        // Shared Database reference
        this.database = FirebaseDatabase.getInstance().getReference();
        // Set Database routes
        routes.put("users", "users");   // key = how to access route internally, value = given route name
        routes.put("channels", "channels");
    }

    public void addUser(User user) {
        // System.out.println("...signing up");
        Map<String, Object> users = new HashMap<>();
        users.put(user.getUsername(), user);
        this.database.child(routes.get("users")).updateChildrenAsync(users);
        // System.out.println("successfully signed up!");
    }

    public User authenticateUser(User user) throws InterruptedException {
        System.out.println("logging in...");
        Semaphore semaphore = new Semaphore(0);     // to wait for the asynchronous call below to complete synchronously
        final User[] retrievedUser = {null};
        this.database.child(routes.get("users")).child(user.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = (String)snapshot.child("password").getValue();
                if (password == null) {
                    System.out.println("Invalid username!");
                } else if (!password.equals(user.getPassword())) {
                    System.out.println("Invalid password!");
                } else {
                    String name = (String)snapshot.child("name").getValue();
                    user.setName(name);
                    retrievedUser[0] = user;
                }
                semaphore.release();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
        semaphore.acquire();
        return retrievedUser[0];
    }

    public void addChannel(Channel channel) {
        Map<String, Object> channels = new HashMap<>();
        channels.put(channel.getName(), channel);
        this.database.child(routes.get("channels")).updateChildrenAsync(channels);
    }

    public void getChannels(User user) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);     // to wait for the asynchronous call below to complete synchronously
        this.database.child(routes.get("channels")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot channel : snapshot.getChildren()) {
                    boolean isMember = false;
                    StringBuilder members = new StringBuilder();
                    for (DataSnapshot member : channel.child("members").getChildren()) {
                        members.append(member.getValue()+" ");
                        if (user.getUsername().equals((String)member.getValue())) {
                            isMember = true;
                        }
                    } if (isMember) {
                        System.out.println(channel.getKey()+":  "+members);
                    } else {
                        members = new StringBuilder();
                    }
                }
                semaphore.release();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
        semaphore.acquire();
    }

    public boolean connect(User user, Connection connection) throws InterruptedException {
        final boolean[] isMember = {false};
        final boolean[] isValid = {false};
        Semaphore semaphore = new Semaphore(0);     // to wait for the asynchronous call below to complete synchronously
        this.database.child(routes.get("channels")).child(connection.getAddress()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot member : snapshot.child("members").getChildren()) {
                    if (user.getUsername().equals((String)member.getValue())) {
                        isMember[0] = true;
                    } else if (connection.getUser().equals((String)member.getValue())) {
                        isValid[0] = true;
                    }
                }
                if (isMember[0] && isValid[0]) {
                    if (connection.getPassword().equals(snapshot.child("password").getValue().toString())) {
                        System.out.println("Connecting to remote server...");
                    } else {
                        System.out.println("Incorrect password.");
                        isValid[0] = false;
                    }
                } else if (!isMember[0] && isValid[0]){
                    System.out.println("Access denied!");
                } else if (!isValid[0] && isMember[0]) {
                    System.out.println("Incorrect username.");
                } else {
                    System.out.println("Incorrect address.");
                }
                semaphore.release();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
        semaphore.acquire();
        return isMember[0] && isValid[0];
    }
}