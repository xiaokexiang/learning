package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.locks.StampedLock;

/**
 * @author xiaokexiang
 * @date 7/6/2020
 */
@Slf4j
public class StampedLockTest2 {
    private static final StampedLock LOCK = new StampedLock();
    private static int x, y;

    public static void main(String[] args) throws InterruptedException {

        new Thread(() -> {
            long stamp = LOCK.readLock();
            try {
                System.out.println("get read lock1");
            } finally {
                LOCK.unlockRead(stamp);
            }
        }, "R1").start();

        new Thread(() -> {
            long stamp = LOCK.readLock();
            try {
                System.out.println("get read lock2");
            } finally {
                LOCK.unlockRead(stamp);
            }
        }, "R2").start();

        new Thread(() -> {
            long stamp = LOCK.writeLock();
            try {
                System.out.println("get write lock");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlockWrite(stamp);
            }
        }, "W1").start();

        new Thread(() -> {
            long stamp = LOCK.writeLock();
            try {
                System.out.println("get write lock2");
                Thread.sleep(200000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlockWrite(stamp);
            }
        }, "W2").start();
    }
}
