import java.io.BufferedReader;
import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abhi on 2018-03-11.
 */
public enum Command {
    EXIT, WHITE_SPACE, CLEAR, LOGIN, LOG_OUT, SIGN_UP, NEW_CHANNEL, MY_CHANNELS, CONNECT, CONNECTED;

    public static Command type(String command) {
        if (command.equals("exit"))
            return Command.EXIT;
        else if (command.equals("clear"))
            return Command.CLEAR;
        else if (command.equals("login"))
            return Command.LOGIN;
        else if (command.equals("log_out"))
            return  Command.LOG_OUT;
        else if (command.equals("sign_up"))
            return Command.SIGN_UP;
        else if (command.equals("new channel"))
            return Command.NEW_CHANNEL;
        else if (command.equals("my channels"))
            return Command.MY_CHANNELS;
        else if (command.length() >= 3 && command.substring(0,3).equals("ssh"))
            return Command.CONNECT;
        else
            return Command.WHITE_SPACE;
    }

    public static void clear() {
        System.out.println("...Terminating the Virtual Machine");
        System.out.println("Please Close manually with Options > Close");
        System.exit(0);
    }

    public static User signUp(BufferedReader input) throws java.io.IOException {
        Console console = System.console();
        if (console == null) {
            // System.out.println("Couldn't get Console instance");
            // System.exit(0);
        }
        System.out.print("Name: ");
        String name = input.readLine();
        System.out.print("Username: ");
        String username = input.readLine();
        System.out.print("Password: ");
        String password = input.readLine();

        // char passwordArray[] = console.readPassword("Password: ");
        // System.out.println();
        //return new User(name, username, new String(passwordArray));

        return new User(name, username, password);

    }

    public static User login(BufferedReader input) throws java.io.IOException {
        Console console = System.console();
        if (console == null) {
            // System.out.println("Couldn't get Console instance");
        }
        System.out.print("Username: ");
        String username = input.readLine();
        System.out.print("Password: ");
        String password = input.readLine();

        // to hide a password when being typed, uncomment the lines below
        // char passwordArray[] = console.readPassword("Password: ");
        // System.out.println();
        //return new User(name, username, new String(passwordArray));
        return new User("", username, password);

    }

    public static Channel newChannel(BufferedReader input, User user) throws java.io.IOException {
        System.out.print("Channel: ");
        String channel = input.readLine();
        System.out.print("Channel Password: ");
        String password = input.readLine();
        System.out.print("Members: ");
        String[] newMembers = input.readLine().split(" ");
        List<String> members = new ArrayList<>(Arrays.asList(user.getUsername()));
        for (int i = 0; i < newMembers.length; i++) {
            members.add(newMembers[i]);
        }
        return new Channel(channel, password, members);
    }

    public static void connect(BufferedReader input) throws java.io.IOException {
        // System.out.println("Good to go!");
    }

    public static Connection getConnection(User user, String username, String address, BufferedReader input) throws java.io.IOException {
        System.out.print("Password: ");
        String password = input.readLine();
        return new Connection(user, username, address, password, true);
    }
}