import java.util.concurrent.CountDownLatch;

/**
 * Created by Abhi on 2018-03-13.
 */

public class UserThread {
    private User user;
    public final CountDownLatch resultReady;
    public Object result;

    public UserThread(User user) {
        this.user = user;
        this.resultReady = new CountDownLatch(1);
    }

    public User getUser() {
        return this.user;
    }

    public void setResult(Object result) {
        this.result = result;
        resultReady.countDown();
    }

    public Object getResult() throws InterruptedException {
        resultReady.await();
        return result;
    }
}
