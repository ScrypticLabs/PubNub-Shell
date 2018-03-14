import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhi on 2018-03-14.
 */

public class Bash {
    private static Map<String, Boolean> outputProducingCommands = new HashMap<>();

    // map of all output producing commands
    static {
        outputProducingCommands.put("ls",true);
    }

    /**
     * Execute a bash command. We can handle complex bash commands including
     * multiple executions (; | && ||), quotes, expansions ($), escapes (\), e.g.:
     *     "cd /abc/def; mv ghi 'older ghi '$(whoami)"
     * @param command
     * @return true if bash got started, but your command may have failed.
     */
    public static boolean executeBashCommand(String command) {
        boolean success = false;
        System.out.println("Executing BASH command:\n   " + command);
        Runtime r = Runtime.getRuntime();
        // Use bash -c so we can handle things like multi commands separated by ; and
        // things like quotes, $, |, and \. My tests show that command comes as
        // one argument to bash, so we do not need to quote it to make it one thing.
        // Also, exec may object if it does not have an executable file as the first thing,
        // so having bash here makes it happy provided bash is installed and in path.
        String[] commands = {"bash", "-c", command};
        try {
            Process p = r.exec(commands);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";

            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }

            b.close();
            success = true;
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
        return success;
    }

    public static String executeBashCommands(List<String> commandsList) {
        Runtime r = Runtime.getRuntime();
        StringBuilder command = new StringBuilder();
        Iterator iterator = commandsList.iterator();

        while (iterator.hasNext()) {
            String _command = iterator.next().toString();
            boolean producesOutput = outputProducingCommands.get(_command) == null ? false: outputProducingCommands.get(_command);
            if (!iterator.hasNext()) {
                command.append(_command+"; ");
            } else if (!producesOutput) {
                command.append(_command+"; ");
            }
        }
        String[] commands = {"bash", "-c", command.toString()};
        StringBuilder output = new StringBuilder();
        try {
            Process p = r.exec(commands);
            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = b.readLine()) != null) {
                output.append(line+"///");
                // System.out.println(line);    // to output line to console
            }
            b.close();
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command");
            e.printStackTrace();
        }
        return output.toString();
    }

}
