package top.leejay.interview.question2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 线程A打印1-10，打印5的时候通知线程B: countDownLatch
 */
@SuppressWarnings("all")
public class Answer4 {
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("thread B working ...");
        }, "线程B").start();

        new Thread(() -> {
            for (int i = 0; i <= 10; i++) {
                System.out.println("i: " + i);
                if (i == 5) {
                    countDownLatch.countDown();
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "线程A").start();
    }
}
