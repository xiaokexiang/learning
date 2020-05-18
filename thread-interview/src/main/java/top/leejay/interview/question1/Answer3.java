package top.leejay.interview.question1;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 交替打印0-100奇偶数: volatile & AtomicInteger 只适用于两个线程 超过两个肯定出问题
 */
@SuppressWarnings("all")
public class Answer3 {
    private static AtomicInteger count = new AtomicInteger(0);
    private static volatile Boolean flag = true;

    public static void main(String[] args) {
        new Thread(() -> {
            while (count.get() <= 100) {
                if (!flag) {
                    System.out.println("奇数: " + count.getAndIncrement());
                    flag = true;
                }
            }
        }, "奇数线程").start();

        new Thread(() -> {
            while (count.get() <= 100) {
                if (flag) {
                    System.out.println("偶数: " + count.getAndIncrement());
                    flag = false;
                }
            }
        }, "偶数线程").start();
    }
}
