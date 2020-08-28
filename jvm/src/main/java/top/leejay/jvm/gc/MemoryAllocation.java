package top.leejay.jvm.gc;

/**
 * @author xiaokexiang
 */
public class MemoryAllocation {

    /**
     * 原则一：大部分情况下，都在eden进行分配，如果eden空间不够会进行一次Minor GC
     * -Xms20m
     * -Xmx20m
     * -Xmn10m
     * -XX:+PrintGCDetails
     * -XX:SurvivorRatio=8(默认)
     * eden: 8m so/s1: 1m
     * old: 10m 6471.68 798.72
     */
    private static final int _1MB = 1024 * 1024;
    public static void main(String[] args) {
        byte[] a1 = new byte[_1MB];
        System.gc();
//        a2 = new byte[2 * _1MB];
//        a3 = new byte[2 * _1MB];
//        // 此时eden为6m，无法分配4m，进行Minor GC，尝试将eden中对象移入s0，s0只有2m，无法移入，最终只好移入老年代
//        a4 = new byte[4 * _1MB];
    }
}
