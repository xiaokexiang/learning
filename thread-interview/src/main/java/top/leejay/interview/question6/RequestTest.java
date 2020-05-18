package top.leejay.interview.question6;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 */
@SuppressWarnings("all")
public class RequestTest {
    public static void main(String[] args) {
        RequestQueue3 requestQueue = new RequestQueue3();
        Thread thread1 = new ClientThread(requestQueue, "client thread");
        Thread thread2 = new ServerThread(requestQueue, "server thread");
        thread1.start();
        thread2.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2s后停止线程
        thread1.interrupt();
        thread2.interrupt();
    }
}
