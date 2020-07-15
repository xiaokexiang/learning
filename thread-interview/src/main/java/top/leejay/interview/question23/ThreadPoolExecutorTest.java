package top.leejay.interview.question23;

import lombok.extern.slf4j.Slf4j;
import top.leejay.interview.question22.ThreadPoolSingleton;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 7/14/2020
 */
@Slf4j
public class ThreadPoolExecutorTest {
    private static final ExecutorService POOL = ThreadPoolSingleton.getInstance();

    public static void main(String[] args) {
        POOL.execute(() -> {
            try {
                log.info("prepare to sleep ...");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        POOL.shutdown();
        log.info("调用shutDown方法 ... ");

        try {
            boolean loop;
            do {
                loop = !POOL.awaitTermination(3, TimeUnit.SECONDS);
            } while (loop);
            log.info("Thread Pool 真正关闭拉 ...");
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
