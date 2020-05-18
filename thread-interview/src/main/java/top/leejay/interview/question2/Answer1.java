package top.leejay.interview.question2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 线程A打印1-10，打印5的时候通知线程B: LockSupprt
 */
@SuppressWarnings("all")
public class Answer1 {
    public static void main(String[] args) {
        Thread threadB = new Thread(() -> {
            LockSupport.park();
            System.out.println("Thread B unpark ...");
        }, "线程B");

        Thread threadA = new Thread(() -> {
            for (int i = 0; i <= 10; i++) {
                System.out.println("i: " + i);
                if (i == 5) {
                    LockSupport.unpark(threadB);
                    // sleep 1s 突出i=5的情况
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "线程A");
        threadB.start();
        threadA.start();
    }
}
