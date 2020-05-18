package top.leejay.interview.question3;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 线程池创建相关
 */
public class ThreadPool {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需要 google的guava jar
        ThreadFactory name =
                new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                name,
                new ThreadPoolExecutor.AbortPolicy());

        Future<String> result = threadPoolExecutor.submit(() -> "hello");
        System.out.println(result.get());
        threadPoolExecutor.shutdown();
    }
}
