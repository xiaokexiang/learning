package top.leejay.interview.question4;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * @author xiaokexiang
 * @date 3/22/2020
 * 模拟N个资源
 */
@Slf4j
public class BoundedResources {

    /**
     * 构建Semaphore
     */
    private final Semaphore semaphore;

    /**
     * 资源数量 N -> permits
     */
    private final int permits;

    public BoundedResources(int permits) {
        this.semaphore = new Semaphore(permits);
        this.permits = permits;
    }

    /**
     * 模拟线程对共享资源的使用
     */
    void use() throws InterruptedException {
        // 获取资源 若无则阻塞等待
        semaphore.acquire();
        // 和ReentrantLock用法相同
        try {
            // 操作共享资源
            doUse();
        } finally {
            semaphore.release();
        }
    }

    private void doUse() throws InterruptedException {
        log.info("Begin: used = {}", permits - semaphore.availablePermits());
        Thread.sleep(new Random().nextInt(5000));
        log.info("End: used = {}", permits - semaphore.availablePermits());
    }
}
