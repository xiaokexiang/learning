package top.leejay.jvm.gc;



/**
 * @author xiaokexiang
 */
public class MemoryAllocation {

    /**
     * 原则一：大部分情况下，都在eden进行分配，如果eden空间不够会进行一次Minor GC
     * -Xms20m
     * -Xmx20ma's
     * -Xmn10m
     * -XX:+PrintGCDetails
     * -XX:SurvivorRatio=8(默认)
     * eden: 8m so/s1: 1m
     * old: 10m
     */
    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {
        byte[] a1 = new byte[_1MB];
        System.gc();
        try {
            Thread.sleep(200000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
