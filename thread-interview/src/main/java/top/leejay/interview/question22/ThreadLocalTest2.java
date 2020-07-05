package top.leejay.interview.question22;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xiaokexiang
 * @date 7/3/2020
 */
public class ThreadLocalTest2 {
    private static final ThreadLocal<HoldCount> readHolds = ThreadLocal.withInitial(HoldCount::new);
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    private HoldCount cacheHoldCount;

    static class HoldCount {
        int count = 0;
        // Use id, not reference, to avoid garbage retention
        final long tid = Thread.currentThread().getId();
    }

    void lock() {
        HoldCount rh = cacheHoldCount;
        if (rh == null || rh.tid != Thread.currentThread().getId()) {
            cacheHoldCount = rh = readHolds.get();
        } else if (rh.count == 0) {
            readHolds.set(rh);
        }
        System.out.println("something ...");
        rh.count++;
    }

    void unlock() {
        HoldCount rh = cacheHoldCount;
        if (rh == null || rh.tid != Thread.currentThread().getId())
            rh = readHolds.get();
        int count = rh.count;
        if (count <= 1) {
            readHolds.remove();
            if (count <= 0)
                throw new Error("12");
        }
        --rh.count;
    }

    // 模拟锁重入
    void test() {
        lock();
        try {
            System.out.println("step 1");
            // 模拟重入读锁
            test2();
        } finally {
            unlock();
        }

    }

    private void test2() {
        lock();
        try {
            System.out.println("step 2");
        } finally {
            unlock();
        }
    }

    // 模拟同一线程先释放锁再获取锁
    private void test3() {
        lock();
        try {
            System.out.println("step 3");
        } finally {
            unlock();
            test4();
        }
    }

    private void test4() {
        lock();
        try {
            System.out.println("step 4");
        } finally {
            unlock();
        }
    }

    public static void main(String[] args) {
        ThreadLocalTest2 localTest2 = new ThreadLocalTest2();
        POOL.execute(localTest2::test3);
        System.out.println("main end ...");
        POOL.shutdown();
    }
}
