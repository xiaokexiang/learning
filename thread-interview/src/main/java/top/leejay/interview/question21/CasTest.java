package top.leejay.interview.question21;

import sun.misc.Unsafe;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

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
    private static AtomicInteger i = new AtomicInteger(0);
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static void main(String[] args) {

        AtomicStampedReference<Object> reference = new AtomicStampedReference<>(new Object(), 123);
    }
}
