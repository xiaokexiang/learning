package top.leejay.interview.question2;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 线程A打印1-10，打印5的时候通知线程B: volatile
 * 感觉这个没有lockSupport的好
 */
@SuppressWarnings("all")
public class Answer2 {
    private static volatile Boolean flag = false;

    @SneakyThrows
    public static void main(String[] args) {

        new Thread(() -> {
            while (true) {
                while (flag) {
                    System.out.println("thread B work...");
                    flag = false;
                }
            }
        }, "线程B").start();

        TimeUnit.SECONDS.sleep(2);

        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                System.out.println("i: " + i);
                if (i == 5) {
                    flag = true;
                    // current thread sleep 1s 突出i=5的情况
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
