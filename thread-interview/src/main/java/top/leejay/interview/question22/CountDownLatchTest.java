package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * @author xiaokexiang
 * @since 2020/7/5
 * 使用CountDownLatch 模拟主线程将任务交给子线程执行，子线程执行完毕，主线程汇总数据（类似ForkJoin？）
 */
@Slf4j
public class CountDownLatchTest {

    private static final CountDownLatch LATCH = new CountDownLatch(2);
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                log.info("子线程1任务开始 ...");
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("子线程1任务结束 ...");
            LATCH.countDown();
        }).start();

        new Thread(() -> {
            try {
                log.info("子线程2任务开始 ...");
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("子线程2任务结束 ...");
            LATCH.countDown();
        }).start();

        new Thread(() -> {
            try {
                log.info("主线程1等待 ...");
                LATCH.await();
                log.info("主线程1被唤醒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                log.info("主线程2等待 ...");
                LATCH.await();
                log.info("主线程2被唤醒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
