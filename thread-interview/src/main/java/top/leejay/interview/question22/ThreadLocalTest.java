package top.leejay.interview.question22;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xiaokexiang
 * @date 7/1/2020
 */
public class ThreadLocalTest {

    // 模拟线程复用的情况,如果不及时清理会导致不符合预期的值.
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    private static final ThreadLocal<Integer> LOCAL = ThreadLocal.withInitial(() -> 0);
    public static void main(String[] args) {
        POOL.execute(() -> {
            Integer integer = LOCAL.get();
            System.out.println("1. get value: " + integer);
            LOCAL.set(123);
        });

        POOL.execute(() -> {
            Integer integer = LOCAL.get();
            System.out.println("2. get value: " + integer);
            LOCAL.remove();
        });

        POOL.execute(() -> {
            Integer integer = LOCAL.get();
            System.out.println("3. get value: " + integer);
            LOCAL.remove();
        });

        POOL.shutdown();
    }
}
