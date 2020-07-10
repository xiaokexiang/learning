package top.leejay.interview.question23;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author xiaokexiang
 * @date 7/9/2020
 */
public class ConcurrentLinkedQueueTest {

    private static ConcurrentLinkedQueue<Integer> QUEUE = new ConcurrentLinkedQueue<>();
    private static final CountDownLatch COUNT = new CountDownLatch(3);
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            QUEUE.offer(11);
            COUNT.countDown();
        }).start();

        new Thread(() -> {
            QUEUE.offer(22);
            COUNT.countDown();
        }).start();

        new Thread(() -> {
            QUEUE.offer(33);
            COUNT.countDown();
        }).start();

        COUNT.await();

        QUEUE.poll();
    }
}
