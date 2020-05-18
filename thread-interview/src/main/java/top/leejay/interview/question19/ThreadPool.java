package top.leejay.interview.question19;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class ThreadPool {
    public static ExecutorService getInstance() {
        return new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(20),
                new CustomizableThreadFactory("Thread-Pool-"),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static void shutdown() {
        getInstance().shutdown();
    }
}
