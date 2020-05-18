package top.leejay.interview.question20;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class MyTask implements Runnable {

    /**
     * 需要执行的任务数
     */
    private static final int PHASE = 5;
    private final CountDownLatch countDownLatch;
    private final CyclicBarrier cyclicBarrier;
    private final int context;
    private static final Random RANDOM = new Random();

    public MyTask(CountDownLatch countDownLatch, CyclicBarrier cyclicBarrier, int context) {
        this.countDownLatch = countDownLatch;
        this.cyclicBarrier = cyclicBarrier;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < PHASE; i++) {
                doPhase(i);
                // 等待三个线程完成第i阶段的工作，再进行i+1阶段
                cyclicBarrier.await();
            }
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        } finally {
            // 数量减一
            countDownLatch.countDown();
        }
    }

    private void doPhase(int i) {
        System.out.println(Thread.currentThread().getName() + ":MyTask:BEGIN:context = " + context + ", phase=" + i);
        try {
            Thread.sleep(RANDOM.nextInt(3000));
        } catch (InterruptedException e) {
        } finally {
            System.out.println(Thread.currentThread().getName() + ":MyTask:END:context = " + context + ", phase=" + i);
        }
    }
}
