import java.io.*;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Abhi on 2018-03-11.
 */

public class Shell {
    private static final String ROOT_DIRECTORY = "PubNub-Demo: ~Shell$  ";
    private String root = ROOT_DIRECTORY;
    private User user = null;
    private Session session = Session.NONE;
    private Database database = new Database();
    private PubNubAPI pubNubAPI = new PubNubAPI(true);
    private Command type = Command.WHITE_SPACE;
    private Connection connection = null;
    private SynchronousQueue<UserThread> blockingQueue = new SynchronousQueue<UserThread>();

    public Shell(SynchronousQueue<UserThread> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    public void begin() throws java.io.IOException{
        String command = "";
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        do {
            if (this.connection != null) {
                root = connection.getUser() + ": ~Shell$  ";
            } else if (this.user != null) {
                root = this.user.getUsername() + ": ~Shell$  ";
            } else {
                root = ROOT_DIRECTORY;
            }
            switch (session) {
                case NONE:
                    System.out.print(root);
                    command = input.readLine();
                    type = Command.type(command);
                    this.handleCommand(command);
                    break;
                case CONSOLE:
                    System.out.print(root);
                    command = input.readLine();
                    this.handleSession(command, input);
                    break;
                default:
                    this.handleSession(command, input);
                    break;
            }

        } while (type != Command.EXIT);
    }

    private void handleCommand(String command) {
        switch(type) {
            case CLEAR:
                Command.clear();
                break;
            case LOGIN:
                if (user == null) {
                    session = Session.DEFAULT;
                } else {
                    System.out.println("Already logged in as "+user.getUser()+"!");
                } break;
            case LOG_OUT:
                if (user == null) {
                    System.out.println("No user currently logged in!");
                } else {
                    user = null;
                    try {
                        blockingQueue.put(new UserThread(user));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    listener.setUser(user);
                } break;
            case SIGN_UP:
                if (user == null) {
                    session = Session.DEFAULT;
                } else {
                    System.out.println("Already logged in as "+user.getUser()+"!");
                }
                break;
            case NEW_CHANNEL:
                if (user == null) {
                    System.out.println("Must log in before creating new channel.");
                } else {
                    session = Session.DEFAULT;
                } break;
            case MY_CHANNELS:
                if (user == null) {
                    System.out.println("Must log in before viewing your channels.");
                } else {
                    session = Session.DEFAULT;
                } break;
            case CONNECT:
                if (user == null) {
                    System.out.println("Must log in before connecting to server.");
                } else {
                    session = Session.DEFAULT;
                } break;
            default:
                break;
        }
    }

    private void handleSession(String command, BufferedReader input) {
        switch(type) {
            case CLEAR:
                Command.clear();
                break;
            case LOGIN:
                try {
                    User user = Command.login(input);
                    this.user = this.database.authenticateUser(user);
//                    listener.setUser(this.user);
                    try {
                        // System.out.println("Entering block queue");
                        blockingQueue.put(new UserThread(this.user));
                        // System.out.println("Done block queue");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.out.println("Failed to retrieve user sign up information!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                session = Session.NONE;
                break;

            case SIGN_UP:
                try {
                    this.user = Command.signUp(input);
                    database.addUser(this.user);
//                    listener.setUser(this.user);
                    try {
                        blockingQueue.put(new UserThread(this.user));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.out.println("Failed to retrieve user sign up information!");
                }
                session = Session.NONE;
                break;
            case NEW_CHANNEL:
                try {
                    Channel channel = Command.newChannel(input, this.user);
                    this.database.addChannel(channel);
                } catch (IOException e) {
                    System.out.println("Failed to create a new channel!");
                }
                session = Session.NONE;
                break;
            case MY_CHANNELS:
                try {
                    this.database.getChannels(this.user);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                session = Session.NONE;
                break;
            case CONNECT:
                String user;
                String channel;
                command = command.substring(4);
                String[] info = command.split("@");
                user = info[0];
                channel = info[1];
                Connection connection;
                try {
                    connection = Command.getConnection(this.user, user, channel, input);
                    if (this.pubNubAPI.connect(this.user, connection, this.database)) {
                        try {
                            Command.connect(input);
                            this.connection = connection;
                            type = Command.CONNECTED;
                            session = Session.CONSOLE;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Failed to get connection credentials!");
                } if (session == Session.DEFAULT) {
                    type = Command.WHITE_SPACE;
                    session = Session.NONE;
                }
                break;
            case CONNECTED:
                this.handleConnection(command, input);
                break;
            default:
                break;
        }
    }

    private void handleConnection(String command, BufferedReader input) {
        if (Command.type(command) == Command.EXIT) {
            session = Session.NONE;
            this.pubNubAPI.disconnect(true);
            this.connection = null;
            System.out.println("Closing remote connection...");
        } else {
            this.pubNubAPI.sendMessage(command, this.user);
        }

    }
}
