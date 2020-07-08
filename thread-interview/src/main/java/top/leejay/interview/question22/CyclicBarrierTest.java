package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xiaokexiang
 * @date 7/6/2020
 */
@Slf4j
public class CyclicBarrierTest {

    // 传入每次屏障之前需要等待的线程数量
    private static final CyclicBarrier BARRIER = new CyclicBarrier(2, () -> {
        log.info("我是每代最后一个线程执行前需要被打印的日志");
    });

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        EXECUTOR.execute(() -> {
            try {
                //CyclicBarrier 保证await
                log.info("doSomething ... ");
                BARRIER.await();
                log.info("continue exec ...");
                BARRIER.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EXECUTOR.execute(() -> {
            try {
                log.info("doSomething ... ");
                BARRIER.await();
                log.info("continue exec ...");
                BARRIER.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EXECUTOR.shutdown();
    }
}
