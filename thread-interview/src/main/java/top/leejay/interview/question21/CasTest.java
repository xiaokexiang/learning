package top.leejay.interview.question21;

import sun.misc.Unsafe;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaokexiang
 * @date 6/4/2020
 */
public class CasTest {
    private static ExecutorService THREAD_POOL = new ThreadPoolExecutor(
            2,
            4,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(16),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy()
    );
    private static volatile int j = 0;
    private static volatile boolean flag = true;
    private static CasUnsafe UNSAFE = new CasUnsafe(0);
    private static final ReentrantLock LOCK = new ReentrantLock();
    public static void main(String[] args) {

        AtomicStampedReference<Object> reference = new AtomicStampedReference<>(new Object(), 123);
        LongAdder longAdder = new LongAdder();

        @sun.misc.Contended
        class Demo {
            volatile long value;
        }
        THREAD_POOL.execute(() -> {
            while (UNSAFE.getValue() < 100) {
                if (flag) {
                    System.out.println(UNSAFE.incrementAndGet());
                    flag = false;
                }
            }
        });
        THREAD_POOL.execute(() -> {
            while (UNSAFE.getValue() < 100) {
                if (!flag) {
                    System.out.println(UNSAFE.incrementAndGet());
                    flag = true;
                }
            }
        });
        THREAD_POOL.shutdown();

        LOCK.lock();
        try {

        }finally {
            LOCK.unlock();
        }
    }
}
