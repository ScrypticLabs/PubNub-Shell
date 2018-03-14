import java.util.concurrent.SynchronousQueue;

/**
 * Created by Abhi on 2018-03-11.
 */

public class Main {
    public static SynchronousQueue<UserThread> blockingQueue = new SynchronousQueue<UserThread>();

    public static void main(String[] args) throws java.io.IOException {
        Shell shell = new Shell(blockingQueue);
        Listener listener = new Listener(blockingQueue);
        listener.start();
        shell.begin();
    }
}
