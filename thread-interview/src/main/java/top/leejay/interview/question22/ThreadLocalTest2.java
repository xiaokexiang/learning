package top.leejay.interview.question22;

/**
 * @author xiaokexiang
 * @date 7/3/2020
 */
public class ThreadLocalTest2 {
    private static final ThreadLocal<HoldCount> local = ThreadLocal.withInitial(HoldCount::new);
    private HoldCount cacheHoldCount;

    static class HoldCount {
        int count = 0;
        // Use id, not reference, to avoid garbage retention
        final long tid = Thread.currentThread().getId();
    }

    void check() {
        HoldCount rh = cacheHoldCount;
        if (rh == null || rh.tid != Thread.currentThread().getId()) {
            cacheHoldCount = rh = local.get();
        } else if (rh.count == 0) {
            local.set(rh);
        }
        System.out.println("something ...");
        rh.count++;
    }

    public static void main(String[] args) {
        ThreadLocalTest2 localTest2 = new ThreadLocalTest2();
        new Thread(localTest2::check, "t1").start();
        new Thread(localTest2::check, "t2").start();
        System.out.println("main end ...");
    }
}
