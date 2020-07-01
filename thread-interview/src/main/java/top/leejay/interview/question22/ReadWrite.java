package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaokexiang
 * @date 6/30/2020
 */
@Slf4j
public class ReadWrite {
    private static final Map<String, String> CACHE = new HashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final ThreadPoolExecutor POOL = ThreadPoolSingleton.getInstance();

    static String get(String key) {
        readLock.lock();
        try {
            return CACHE.get(key);
        } finally {
            readLock.unlock();
        }
    }

    static void put(String key, String value) {
        writeLock.lock();
        try {
            CACHE.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        POOL.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            put("hello", "world");
        });

        POOL.execute(() -> {
            readLock.lock();
            try {
                put("what", "name");
            } finally {
                readLock.unlock();
            }
        });

        POOL.execute(() -> {
            String hello = get("hello");
            log.info(hello);
        });

        POOL.shutdown();
    }
}
