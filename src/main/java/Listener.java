import java.util.concurrent.SynchronousQueue;

/**
 * Created by Abhi on 2018-03-13.
 */
public class Listener extends Thread {
    public SynchronousQueue<UserThread> blockingQueue = new SynchronousQueue<UserThread>();

    public Listener(SynchronousQueue<UserThread> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run(){
        PubNubAPI pubNubAPI = new PubNubAPI(false);
            UserThread userThread = null;
            try {
                    // System.out.println("Second Thread: block queue entered");
                    userThread = blockingQueue.take();
                    userThread.setResult(true);
                    User user = userThread.getUser();
                    if (user != null) {
                        // System.out.println("Second: connecting to user...");
                        pubNubAPI.disconnect(false);
                        pubNubAPI.connect(user);
                    } else {
                        pubNubAPI.disconnect(false);
                        // System.out.println("Second: disconnecting...");
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}

