package top.leejay.interview.question22;

import lombok.SneakyThrows;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaokexiang
 * @since 2020/7/4
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html
 * 读写锁的降级
 */
public class CachedDate {
    Object data;
    volatile boolean cacheValid;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    @SneakyThrows
    void processCachedData() {
        // 先获取读锁
        rwl.readLock().lock();
        // 判断cacheValid即缓存是否可用
        if (!cacheValid) {
            // 到这里说明cache可用准备写值
            // 需要先释放读锁在获取写锁
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try {
                // 需要再次简要cacheValid，防止其他线程在此期间改过该值
                // 在use方法之前获取写锁写入data值及修改cacheValid状态
                if (!cacheValid) {
                    data = System.currentTimeMillis();
                    System.out.println("Thread: d " + Thread.currentThread().getName() + ", data: " + data);
                    cacheValid = true;
                }
                // 这里就是锁降级。在写锁释放之前先获取读锁。
                rwl.readLock().lock();
            } finally {
                // 释放写锁
                rwl.writeLock().unlock();
            }
        }
        try {
            // 对缓存数据进行打印
            use(data);
        } finally {
            // 最终释放读锁
            rwl.readLock().unlock();
        }
    }

    // 只是打印缓存值
    void use(Object data) {
        System.out.println("Thread: d " + Thread.currentThread().getName() + ", use cache data: " + data);
    }

    public static void main(String[] args) {
        CachedDate cachedDate = new CachedDate();
        new Thread(cachedDate::processCachedData, "B").start();
        new Thread(cachedDate::processCachedData, "A").start();
    }
}
