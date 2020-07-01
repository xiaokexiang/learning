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

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(2);
            POOL.execute(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("Current Thread: " + Thread.currentThread().getName() + " Get Lock");
                    Thread.sleep(6 * 1000);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        POOL.execute(() -> {
            try {
                semaphore.acquire();
                System.out.println("Current Thread: " + Thread.currentThread().getName() + " Get Lock");
                Thread.sleep(100 * 1000);
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        POOL.execute(() -> {
            try {
                semaphore.acquire();
                System.out.println("Current Thread: " + Thread.currentThread().getName() + " Get Lock");
                Thread.sleep(120 * 1000);
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(2000L);
        POOL.execute(() -> {
            // 模拟无锁线程释放共享锁的情况，查看state是否改变,结果是
            System.out.println("start .. ");
            semaphore.release();
        });
        POOL.shutdown();
    }
}
