package top.leejay.interview.question22;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaokexiang
 * @since 2020/7/3
 */
public class ReadWrite2 {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    void first() {
        readLock.lock();
        try {
            System.out.println("first");
            Thread.sleep(1000 * 600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
    }
    void test1() {
        readLock.lock();
        try {

            System.out.println("test1");
            test2();
        } finally {
            readLock.unlock();
        }
    }

    void test2() {
        readLock.lock();
        try {
            System.out.println("test2");
        } finally {
            readLock.unlock();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        ReadWrite2 readWrite2 = new ReadWrite2();
        Thread first = new Thread(readWrite2::first, "first");
        Thread re = new Thread(readWrite2::test1, "re");
        first.start();
        Thread.sleep(2000);
        re.start();
    }
}
