package top.leejay.interview.question2;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 线程A打印1-10，打印5的时候通知线程B: wait & nofity synchronized
 */
@SuppressWarnings("all")
public class Answer3 {
    private static final Object obj = new Object();

    @SneakyThrows
    public static void main(String[] args) {
        new Thread(() -> {
            while (true) {
                synchronized (obj) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread b working ...");
                }
            }
        }, "线程B").start();

        TimeUnit.SECONDS.sleep(5);
        new Thread(() -> {
            synchronized (obj) {
                for (int i = 0; i <= 10; i++) {
                    if (i == 5) {
                        obj.notify();
                    }
                }
            }
        }, "线程A").start();
    }
}
