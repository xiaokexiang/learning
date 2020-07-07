package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.locks.StampedLock;

/**
 * @author xiaokexiang
 * @date 7/6/2020
 */
@Slf4j
public class StampedLockTest {
    private static final StampedLock LOCK = new StampedLock();
    private static int x, y;

    static void add() {
        long stamp = LOCK.writeLock();
        try {
            x += 1;
            y += 1;
        } finally {
            LOCK.unlockWrite(stamp);
        }
    }

    static void print() {
        // 尝试乐观读
        long stamp = LOCK.tryOptimisticRead();
        // 如果stamp修改了，这时再加写锁
        int currentX = x, currentY = y;
        if (!LOCK.validate(stamp)) {
            log.info("value has changed ...");
            stamp = LOCK.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                LOCK.unlockRead(stamp);
            }
        }
        log.info("x: {}, y: {}", currentX, currentY);
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            new Thread(StampedLockTest::add).start();
            Thread.sleep(new Random().nextInt(2) * 1000);
            new Thread(StampedLockTest::print).start();
        }
    }
}
