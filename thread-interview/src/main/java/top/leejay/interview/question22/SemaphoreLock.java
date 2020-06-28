package top.leejay.interview.question22;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaokexiang
 * @date 6/28/2020
 * AQS共享锁的体现-semaphore
 */
public class SemaphoreLock {
    private static final ThreadPoolExecutor POOL = ThreadPoolSingleton.getInstance();

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < 3; i++) {
            POOL.execute(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("Current Thread: " + Thread.currentThread().getName() + " Get Lock");
                    Thread.sleep(60 * 1000);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        POOL.shutdown();
    }
}
