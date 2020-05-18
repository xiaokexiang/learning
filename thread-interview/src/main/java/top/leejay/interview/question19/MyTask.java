package top.leejay.interview.question19;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class MyTask implements Runnable {

    private final CountDownLatch countDownLatch;
    private final int context;
    private static final Random RANDOM = new Random();

    public MyTask(CountDownLatch countDownLatch, int context) {
        this.countDownLatch = countDownLatch;
        this.context = context;
    }

    @Override
    public void run() {
        doTask();
        countDownLatch.countDown();
    }

    private void doTask() {
        System.out.println(Thread.currentThread().getName() + ":MyTask:BEGIN:context = " + context);
        try {
            Thread.sleep(RANDOM.nextInt(3000));
        } catch (InterruptedException e) {
        } finally {
            System.out.println(Thread.currentThread().getName() + ":MyTask:END:context = " + context);
        }
    }
}
