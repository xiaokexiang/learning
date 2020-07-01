package top.leejay.interview.question22;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaokexiang
 * @date 7/1/2020
 * 测试Synchronized 和 semaphore的区别
 * semaphore permit > 1时操作共享变量会出现数据不一致情况出现
 */
@Slf4j
public class SemaphoreAndSyn {

    private static int state = 0;
    private static final Semaphore lock = new Semaphore(2);
    private static final ThreadPoolExecutor POOL = ThreadPoolSingleton.getInstance();

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            POOL.execute(() -> {
                lock.acquireUninterruptibly();
                log.info("Thread name: {}, value: {}", Thread.currentThread().getName(), state++);
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.release();
            });
        }
        POOL.shutdown();
    }
}
