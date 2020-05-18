package top.leejay.interview.question19;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 * CountDownLatch： 等待指定次数的countDown方法被调用 await() countDown() 常用于倒数计数器，数值变为0，立刻返回
 */
public class Main {
    public static void main(String[] args) {
        ExecutorService executor = ThreadPool.getInstance();
        System.out.println("Main Begin ...");
        CountDownLatch countDownLatch = new CountDownLatch(10);

        try {
            for (int i = 0; i < 10; i++) {
                executor.execute(new MyTask(countDownLatch, i));
            }
            System.out.println("await ...");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        System.out.println("Main End ...");
    }
}
