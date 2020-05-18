package top.leejay.interview.question1;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 交替打印0-100奇偶数: ReentrantLock
 */
public class Answer1 {

    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
    public static void main(String[] args) {
        REENTRANT_LOCK.lock();
        try {
            for (int i = 0; i <= 100; i++) {
                if (i % 2 == 0) {
                    System.out.println("偶数: " + i);
                } else {
                    System.out.println("奇数: " + i);
                }
            }
        } finally {
            REENTRANT_LOCK.unlock();
        }
    }
}
