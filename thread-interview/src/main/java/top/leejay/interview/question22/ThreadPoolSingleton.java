package top.leejay.interview.question22;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 6/23/2020
 */
public class ThreadPoolSingleton {

    public static ThreadPoolExecutor getInstance() {
        return Holder.THREAD_POOL_EXECUTOR;
    }

    private static class Holder {
        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
                16,
                32,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                new CustomizableThreadFactory("Thread-Pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
