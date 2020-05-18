package top.leejay.interview.question16;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 * 使用线程池修改Worker-Thread模式
 */
public class Main {
    public static void main(String[] args) {
        ExecutorService executor = new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new CustomizableThreadFactory("Thread-pool-"),
                new ThreadPoolExecutor.AbortPolicy()
        );

        try {
            new ClientThread("Alice", executor).start();
            new ClientThread("Booby", executor).start();
            new ClientThread("Chris", executor).start();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
