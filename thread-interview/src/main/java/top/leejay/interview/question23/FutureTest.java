package top.leejay.interview.question23;

import top.leejay.interview.question22.ThreadPoolSingleton;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author xiaokexiang
 * @date 7/16/2020
 */
public class FutureTest {
    private static final ExecutorService POOL = ThreadPoolSingleton.getInstance();

    public static void main(String[] args) {
        Future<String> future = POOL.submit(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello world";
        });
        // 判断任务是否结束
        while (!future.isDone()) {
            try {
                System.out.println("Thread is not complete, prepare to sleep");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        future.cancel(true);
        try {
            // 获取任务
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        POOL.shutdown();
    }
}
