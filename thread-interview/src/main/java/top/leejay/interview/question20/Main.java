package top.leejay.interview.question20;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 * cyclicBarrier 周期性(cyclic)的创建屏障(barrier),在屏障解除之前，碰到屏障的线程是无法继续前进的。
 * 只有到达屏障出的线程个数达到了构造函数的个数.
 * tip: 启动三个线程，分别执行5个任务
 */
@SuppressWarnings("all")
public class Main {
    public static void main(String[] args) {
        // 启动三个线程
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Barrier action ... ");
            }
        };

        // 使线程步调一致
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, runnable);
        // 用于确认工作是否结束
        CountDownLatch countDownLatch = new CountDownLatch(3);
        try {
            for (int i = 0; i < 3; i++) {
                executorService.execute(new MyTask(countDownLatch, cyclicBarrier, i));
            }
            System.out.println("await ... ");
            // 等待3个线程完成工作
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            System.out.println("end ... ");
        }
    }
}
